using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Patching;

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