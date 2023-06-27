using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression;

internal interface ITestSuppressionDecider
{
    (bool shouldBeSuppressed, ITestSelector testSelector) Decide(string testSelectorQuery, bool inclusionMode, IReadOnlyDictionary<string, ITestSelector> testSelectors);
}