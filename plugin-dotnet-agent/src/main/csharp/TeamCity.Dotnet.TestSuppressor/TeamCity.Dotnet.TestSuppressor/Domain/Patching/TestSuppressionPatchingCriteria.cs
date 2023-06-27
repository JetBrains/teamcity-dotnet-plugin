using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Patching;

internal record TestSuppressionPatchingCriteria(
    IReadOnlyDictionary<string, ITestSelector> TestSelectors,
    bool InclusionMode
) : IAssemblyPatchingCriteria;
