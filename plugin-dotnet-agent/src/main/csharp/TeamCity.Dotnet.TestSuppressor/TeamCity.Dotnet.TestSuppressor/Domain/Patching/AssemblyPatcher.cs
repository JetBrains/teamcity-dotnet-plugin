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
            "Assembly patched successfully: {OriginalAssemblyPath}, backup: {BackupAssemblyPath}, symbols: {HasSymbols}\n" +
            "Affected {AffectedTypes} type(s) and {AffectedMethods} method(s)",
            savingResult.OriginalAssemblyPath,
            savingResult.BackupAssemblyPath,
            savingResult.BackupSymbolsPath == null,
            mutationResult.AffectedTypes,
            mutationResult.AffectedMethods
        );
        
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

    private async Task<SavingResult> SaveAssemblyAsync(IDotnetAssembly assembly, string originalAssemblyPath)
    {
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

        // replace the original assembly with the modified one
        _fileSystem.File.Delete(originalAssemblyPath);
        _fileSystem.File.Move(tmpAssemblyPath, originalAssemblyPath);

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

