using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Patching;

internal class AssemblyPatcher : IAssemblyPatcher
{
    private const string BackupFilePostfix = "_backup";
    private const string TempFilePostfix = "_tmp";
    private readonly IEnumerable<IAssemblyMutator> _mutators;
    private readonly IDotnetAssemblyLoader _assemblyLoader;
    private readonly IFileSystem _fileSystem;
    private readonly IFileCopier _fileCopier;
    private readonly ILogger<AssemblyPatcher> _logger;

    public AssemblyPatcher(
        IEnumerable<IAssemblyMutator> mutators,
        IDotnetAssemblyLoader assemblyLoader,
        IFileSystem fileSystem,
        IFileCopier fileCopier,
        ILogger<AssemblyPatcher> logger)
    {
        _mutators = mutators;
        _assemblyLoader = assemblyLoader;
        _fileSystem = fileSystem;
        _fileCopier = fileCopier;
        _logger = logger;
    }

    public async Task<AssemblyPatchingResult> TryPatchAsync(IFileInfo assemblyFile, IAssemblyPatchingCriteria criteria)
    {
        _logger.LogInformation("Trying to patch assembly: {AssemblyFile}", assemblyFile.FullName);

        var mutationResult = AssemblyMutationResult.Empty;
        
        var modifiedAssemblyResult = await TryModifyAssembly(assemblyFile.FullName, async assembly =>
        {
            var mutator = SelectMutator(criteria);
            mutationResult = await mutator.MutateAsync(assembly, criteria);
            return !mutationResult.IsEmpty;
        });

        if (modifiedAssemblyResult.IsModified && !mutationResult.IsEmpty)
        {
            _logger.LogInformation(
                "Assembly patched successfully: {OriginalAssemblyPath}, backup: {BackupAssemblyPath}, symbols: {HasSymbols}\n" +
                "Affected {AffectedTypes} type(s) and {AffectedMethods} method(s)",
                modifiedAssemblyResult.OriginalAssemblyPath,
                modifiedAssemblyResult.BackupAssemblyPath,
                modifiedAssemblyResult.HasBackupSymbols,
                mutationResult.AffectedTypes,
                mutationResult.AffectedMethods
            );
            return AssemblyPatchingResult.Patched(
                assemblyPath: modifiedAssemblyResult.OriginalAssemblyPath,
                backupAssemblyPath: modifiedAssemblyResult.BackupAssemblyPath,
                symbolsPath: modifiedAssemblyResult.OriginalSymbolsPath,
                backupSymbolsPath: modifiedAssemblyResult.BackupSymbolsPath,
                mutationResult: mutationResult
            );
        }

        _logger.LogInformation("No changes were made to the assembly: {AssemblyFile}", assemblyFile.FullName);
        return AssemblyPatchingResult.NotPatched(assemblyFile.FullName);
    }

    private IDotnetAssembly LoadAssembly(string assemblyPath)
    {
        var hasSymbols = _fileSystem.File.Exists(_fileSystem.Path.ChangeExtension(assemblyPath, FileExtension.Symbols));
        var assembly = _assemblyLoader.LoadAssembly(assemblyPath, hasSymbols);
        if (assembly == null)
        {
            throw new Exception($"Failed to load assembly {assemblyPath}");
        }

        return assembly;
    }

    private IAssemblyMutator SelectMutator(IAssemblyPatchingCriteria criteria) =>
        _mutators.First(m => m.PatchingCriteriaType == criteria.GetType());

    private async Task<ModifiedAssemblyResult> TryModifyAssembly(string originalAssemblyPath, Func<IDotnetAssembly, Task<bool>> modifyAction)
    {
        ModifiedAssemblyResult modifiedAssemblyResult;
        
        using (var assembly = LoadAssembly(originalAssemblyPath))
        {
            if (!await modifyAction(assembly))
            {
                return ModifiedAssemblyResult.Empty;
            }
            modifiedAssemblyResult = await SaveModifiedAssemblyAsync(assembly);
        }
        
        EnableModifiedAssembly(modifiedAssemblyResult);

        return modifiedAssemblyResult;
    }

    private async Task<ModifiedAssemblyResult> SaveModifiedAssemblyAsync(IDotnetAssembly assembly)
    {
        var originalAssemblyPath = assembly.FullPath;
        var backupAssemblyPath = GetBackupFilePath(originalAssemblyPath);
        var tmpAssemblyPath = GetTempFilePath(originalAssemblyPath);
        
        // backup the original assembly
        await _fileCopier.CopyFile(originalAssemblyPath, backupAssemblyPath);

        // make a tmp copy of the original assembly
        await _fileCopier.CopyFile(originalAssemblyPath, tmpAssemblyPath);
        
        // deal with debug symbols
        var originalSymbolsPath = _fileSystem.Path.ChangeExtension(originalAssemblyPath, FileExtension.Symbols);
        var backupSymbolsPath = GetBackupFilePath(originalSymbolsPath);
        var hasSymbols = assembly.HasSymbols && _fileSystem.File.Exists(originalSymbolsPath);
        if (hasSymbols)
        {
            await _fileCopier.CopyFile(originalSymbolsPath, backupSymbolsPath);
        }

        // save the modified assembly on disk in tmp location and preserve debug symbols if available
        assembly.SaveTo(tmpAssemblyPath, hasSymbols);
        
        return hasSymbols
            ? new ModifiedAssemblyResult(true, tmpAssemblyPath, originalAssemblyPath, backupAssemblyPath, originalSymbolsPath, backupSymbolsPath)
            : new ModifiedAssemblyResult(true, tmpAssemblyPath, originalAssemblyPath, backupAssemblyPath);
    }
    
    private static string GetBackupFilePath(string originalPath) => $"{originalPath}{BackupFilePostfix}";
    
    private static string GetTempFilePath(string originalPath) => $"{originalPath}{TempFilePostfix}";

    private void EnableModifiedAssembly(ModifiedAssemblyResult modifiedAssemblyResult)
    {
        // replace the original assembly with the modified one
        _fileSystem.File.Delete(modifiedAssemblyResult.OriginalAssemblyPath);
        _fileSystem.File.Move(modifiedAssemblyResult.ModifiedAssemblyPath, modifiedAssemblyResult.OriginalAssemblyPath);
    }

    private readonly record struct ModifiedAssemblyResult(
        bool IsModified,
        string ModifiedAssemblyPath,
        string OriginalAssemblyPath,
        string BackupAssemblyPath,
        string? OriginalSymbolsPath = null,
        string? BackupSymbolsPath = null
    )
    {
        public static ModifiedAssemblyResult Empty => new();

        public bool HasBackupSymbols => BackupSymbolsPath != null;
    }
}

