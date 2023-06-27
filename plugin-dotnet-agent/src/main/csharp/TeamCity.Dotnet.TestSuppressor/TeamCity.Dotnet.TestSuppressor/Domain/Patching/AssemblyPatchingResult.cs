namespace TeamCity.Dotnet.TestSuppressor.Domain.Patching;

internal record AssemblyPatchingResult(
    bool IsAssemblyPatched,
    string AssemblyPath,
    string BackupAssemblyPath,
    string? SymbolsPath,
    string? BackupSymbolsPath,
    AssemblyMutationResult? MutationResult)
{
    public bool HasSymbols => !string.IsNullOrEmpty(SymbolsPath) || !string.IsNullOrEmpty(BackupSymbolsPath);

    public static AssemblyPatchingResult NotPatched(string assemblyPath) =>
        new(false, assemblyPath, string.Empty,  null,null, AssemblyMutationResult.Empty);

    public static AssemblyPatchingResult Patched(
        string assemblyPath, string backupAssemblyPath, string? symbolsPath, string? backupSymbolsPath, AssemblyMutationResult? mutationResult) =>
        new(true, assemblyPath, backupAssemblyPath, symbolsPath, backupSymbolsPath, mutationResult);
}