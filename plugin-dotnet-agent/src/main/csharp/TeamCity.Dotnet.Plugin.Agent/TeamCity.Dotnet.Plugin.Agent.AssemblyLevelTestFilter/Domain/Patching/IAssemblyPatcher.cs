using System.IO.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal interface IAssemblyPatcher
{
    Task<AssemblyPatchingResult> TryPatchAsync(IFileInfo assemblyFile, IAssemblyPatchingCriteria criteria);
}