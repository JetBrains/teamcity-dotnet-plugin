using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal interface IAssemblyMutator
{
    Type PatchingCriteriaType { get; }
    
    Task<AssemblyMutationResult> MutateAsync(IDotnetAssembly assembly, IAssemblyPatchingCriteria criteria);
}

internal interface IAssemblyMutator<in TPatchingCriteria> : IAssemblyMutator
    where TPatchingCriteria : IAssemblyPatchingCriteria
{
    Task<AssemblyMutationResult> MutateAsync(IDotnetAssembly assembly, TPatchingCriteria criteria);
}