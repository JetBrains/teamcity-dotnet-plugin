using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Patching;

internal record TestSuppressionPatchingCriteria(
    IReadOnlyDictionary<string, TestSelector> TestSelectors,
    bool InclusionMode
) : IAssemblyPatchingCriteria;
