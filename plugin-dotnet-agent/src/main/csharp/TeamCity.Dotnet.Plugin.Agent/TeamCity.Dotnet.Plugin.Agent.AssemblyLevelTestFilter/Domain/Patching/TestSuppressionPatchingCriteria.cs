using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal record TestSuppressionPatchingCriteria(
    IReadOnlyDictionary<string, ITestsSelector> TestSelectors,
    bool InclusionMode
) : IAssemblyPatchingCriteria;