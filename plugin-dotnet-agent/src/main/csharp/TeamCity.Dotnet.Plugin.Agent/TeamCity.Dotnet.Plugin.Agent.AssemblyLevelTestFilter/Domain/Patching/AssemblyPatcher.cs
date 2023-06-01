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
using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal class AssemblyPatcher : IAssemblyPatcher
{
    private readonly IEnumerable<IAssemblyMutator> _mutators;
    private readonly ILogger<AssemblyPatcher> _logger;

    public AssemblyPatcher(IEnumerable<IAssemblyMutator> mutators, ILogger<AssemblyPatcher> logger)
    {
        _mutators = mutators;
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

    private static AssemblyDefinition LoadAssembly(string assemblyPath)
    {
        var assemblyResolver = new DefaultAssemblyResolver();
        assemblyResolver.AddSearchDirectory(AppDomain.CurrentDomain.BaseDirectory);
        return AssemblyDefinition.ReadAssembly(assemblyPath, new ReaderParameters
        {
            AssemblyResolver = assemblyResolver,
            ReadSymbols = true,                     // read debug symbols if available
        });
    }

    private IAssemblyMutator SelectMutator(IAssemblyPatchingCriteria criteria) =>
        _mutators.First(m => m.GetType().GetInterfaces().First().GetGenericArguments()[0] == criteria.GetType());

    private static async Task<SavingResult> SaveAssemblyAsync(AssemblyDefinition assembly, string originalAssemblyPath)
    {
        var backupAssemblyPath = originalAssemblyPath + "_backup";
        var tmpAssemblyPath = originalAssemblyPath + "_tmp";
        
        // backup the original assembly
        await CopyFile(originalAssemblyPath, backupAssemblyPath);

        // make a tmp copy of the original assembly
        await CopyFile(originalAssemblyPath, tmpAssemblyPath);
        
        // deal with debug symbols
        var originalSymbolsPath = Path.ChangeExtension(originalAssemblyPath, ".pdb");
        var backupSymbolsPath = originalSymbolsPath + "_backup";
        var hasSymbols = File.Exists(originalSymbolsPath);
        if (hasSymbols)
        {
            await CopyFile(originalSymbolsPath, backupSymbolsPath);
        }

        // save the modified assembly on disk in tmp location and preserve debug symbols if available
        await using (var destinationStream = File.Create(tmpAssemblyPath))
        {
            assembly.Write(destinationStream, new WriterParameters { WriteSymbols = true });
        }

        // replace the original assembly with the modified one
        File.Delete(originalAssemblyPath);
        File.Move(tmpAssemblyPath, originalAssemblyPath);

        if (!hasSymbols)
        {
            originalSymbolsPath = null;
            backupSymbolsPath = null;
        }

        return new SavingResult(originalAssemblyPath, backupAssemblyPath, originalSymbolsPath, backupSymbolsPath);
    }

    private static async Task CopyFile(string source, string target)
    {
        await using var sourceStream = File.OpenRead(source);
        await using var destinationStream = File.Create(target);
        await sourceStream.CopyToAsync(destinationStream);
    }
    
    private record struct SavingResult(
        string OriginalAssemblyPath,
        string BackupAssemblyPath,
        string? OriginalSymbolsPath,
        string? BackupSymbolsPath
    );
}

