using System.IO.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Patching;

internal interface IAssemblyPatcher
{
    Task<AssemblyPatchingResult> TryPatchAsync(IFileInfo assemblyFile, IAssemblyPatchingCriteria criteria);
}