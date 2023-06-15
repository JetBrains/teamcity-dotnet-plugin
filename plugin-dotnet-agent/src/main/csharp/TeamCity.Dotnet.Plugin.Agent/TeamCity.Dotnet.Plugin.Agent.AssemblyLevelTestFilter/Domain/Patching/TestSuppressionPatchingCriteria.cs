using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal record TestSuppressionPatchingCriteria(
    IReadOnlyDictionary<string, ITestSelector> TestSelectors,
    bool InclusionMode
) : IAssemblyPatchingCriteria;
