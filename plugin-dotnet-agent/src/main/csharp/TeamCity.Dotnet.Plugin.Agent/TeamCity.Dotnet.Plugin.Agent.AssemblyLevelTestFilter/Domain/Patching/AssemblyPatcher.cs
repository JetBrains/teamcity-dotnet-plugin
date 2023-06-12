/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal class AssemblyPatcher : IAssemblyPatcher
{
    private const string BackupFilePostfix = "_backup";
    private const string TempFilePostfix = "_tmp";
    private readonly IEnumerable<IAssemblyMutator> _mutators;
    private readonly IFileSystem _fileSystem;
    private readonly IDotnetAssemblyLoader _assemblyLoader;
    private readonly ILogger<AssemblyPatcher> _logger;

    public AssemblyPatcher(
        IEnumerable<IAssemblyMutator> mutators,
        IFileSystem fileSystem,
        IDotnetAssemblyLoader assemblyLoader,
        ILogger<AssemblyPatcher> logger)
    {
        _mutators = mutators;
        _fileSystem = fileSystem;
        _assemblyLoader = assemblyLoader;
        _logger = logger;
    }

    public async Task<AssemblyPatchingResult> TryPatchAsync(FileInfo assemblyFile, IAssemblyPatchingCriteria criteria)
    {
        _logger.LogDebug("Patching assembly: {AssemblyFile}", assemblyFile.FullName);

        using var assembly = LoadAssembly(assemblyFile.FullName);

        var mutator = SelectMutator(criteria);
        
        var mutationResult = await mutator.MutateAsync(assembly, criteria);
        if (mutationResult is { AffectedTypes: 0, AffectedMethods: 0 })
        {
            _logger.LogInformation("No changes were made to the assembly: {AssemblyFile}", assemblyFile.FullName);
            return AssemblyPatchingResult.NotPatched(assemblyFile.FullName);
        }

        var savingResult = await SaveAssemblyAsync(assembly, assemblyFile.FullName);
        
        _logger.LogInformation(
            "Patched assembly: {OriginalAssemblyPath}, backup: {BackupAssemblyPath}, symbols: {HasSymbols}",
            savingResult.OriginalAssemblyPath, savingResult.BackupAssemblyPath, savingResult.BackupSymbolsPath == null);
        _logger.LogInformation("Affected {AffectedTypes} type(s) and {AffectedMethods} method(s)", 
            mutationResult.AffectedTypes, mutationResult.AffectedMethods);
        
        return AssemblyPatchingResult.Patched(
            assemblyPath: savingResult.OriginalAssemblyPath,
            backupAssemblyPath: savingResult.BackupAssemblyPath,
            symbolsPath: savingResult.OriginalSymbolsPath,
            backupSymbolsPath: savingResult.BackupSymbolsPath,
            mutationResult: mutationResult
        );
    }

    private IDotnetAssembly LoadAssembly(string assemblyPath)
    {
        var hasSymbols = _fileSystem.FileExists(_fileSystem.ChangeFileExtension(assemblyPath, FileExtension.Symbols));
        var assembly = _assemblyLoader.LoadAssembly(assemblyPath, hasSymbols);
        if (assembly == null)
        {
            throw new Exception($"Failed to load assembly {assemblyPath}");
        }

        return assembly;
    }

    private IAssemblyMutator SelectMutator(IAssemblyPatchingCriteria criteria) =>
        _mutators.First(m => m.GetType().GetInterfaces().First().GetGenericArguments()[0] == criteria.GetType());

    private async Task<SavingResult> SaveAssemblyAsync(IDotnetAssembly assembly, string originalAssemblyPath)
    {
        var backupAssemblyPath = GetBackupFilePath(originalAssemblyPath);
        var tmpAssemblyPath = GetTempFilePath(originalAssemblyPath);
        
        // backup the original assembly
        await _fileSystem.CopyFile(originalAssemblyPath, backupAssemblyPath);

        // make a tmp copy of the original assembly
        await _fileSystem.CopyFile(originalAssemblyPath, tmpAssemblyPath);
        
        // deal with debug symbols
        var originalSymbolsPath = _fileSystem.ChangeFileExtension(originalAssemblyPath, FileExtension.Symbols);
        var backupSymbolsPath = GetBackupFilePath(originalSymbolsPath);
        var hasSymbols = assembly.HasSymbols && _fileSystem.FileExists(originalSymbolsPath);
        if (hasSymbols)
        {
            await _fileSystem.CopyFile(originalSymbolsPath, backupSymbolsPath);
        }

        // save the modified assembly on disk in tmp location and preserve debug symbols if available
        await using (var destinationStream = _fileSystem.CreateFile(tmpAssemblyPath))
        {
            assembly.Write(destinationStream, hasSymbols);
        }

        // replace the original assembly with the modified one
        _fileSystem.DeleteFile(originalAssemblyPath);
        _fileSystem.MoveFile(tmpAssemblyPath, originalAssemblyPath);

        if (!hasSymbols)
        {
            originalSymbolsPath = null;
            backupSymbolsPath = null;
        }

        return new SavingResult(originalAssemblyPath, backupAssemblyPath, originalSymbolsPath, backupSymbolsPath);
    }
    
    private static string GetBackupFilePath(string originalPath) => $"{originalPath}{BackupFilePostfix}";
    
    private static string GetTempFilePath(string originalPath) => $"{originalPath}{TempFilePostfix}";

    private record struct SavingResult(
        string OriginalAssemblyPath,
        string BackupAssemblyPath,
        string? OriginalSymbolsPath,
        string? BackupSymbolsPath
    );
}

