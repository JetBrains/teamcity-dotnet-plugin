using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal interface IAssemblyMutator {}

internal interface IAssemblyMutator<in TPatchingCriteria> : IAssemblyMutator
    where TPatchingCriteria : IAssemblyPatchingCriteria
{
    Task<AssemblyMutationResult> MutateAsync(AssemblyDefinition assembly, TPatchingCriteria criteria);
}