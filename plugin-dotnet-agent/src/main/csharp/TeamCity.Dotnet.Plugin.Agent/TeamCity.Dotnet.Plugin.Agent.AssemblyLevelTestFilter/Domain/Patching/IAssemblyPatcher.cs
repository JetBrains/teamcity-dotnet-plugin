namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal interface IAssemblyPatcher
{
    Task<AssemblyPatchingResult> TryPatchAsync(FileInfo assemblyFile, IAssemblyPatchingCriteria criteria);
}