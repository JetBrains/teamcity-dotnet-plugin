namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal record AssemblyPatchingResult(bool IsAssemblyPatched, string AssemblyPath, string BackupPath, AssemblyMutationResult MutationResult)
{
    public static AssemblyPatchingResult NotPatched(string assemblyPath) =>
        new AssemblyPatchingResult(false, assemblyPath, string.Empty, AssemblyMutationResult.Empty);
    
    public static AssemblyPatchingResult Patched(string originalAssemblyPath, string patchedAssemblyPath, AssemblyMutationResult mutationResult) =>
        new AssemblyPatchingResult(true, originalAssemblyPath, patchedAssemblyPath, mutationResult);
}