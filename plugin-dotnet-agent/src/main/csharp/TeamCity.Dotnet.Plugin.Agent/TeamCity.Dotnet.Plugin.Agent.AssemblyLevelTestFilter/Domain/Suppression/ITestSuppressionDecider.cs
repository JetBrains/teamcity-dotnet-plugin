using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal interface ITestSuppressionDecider
{
    (bool shouldBeSuppressed, ITestSelector testSelector) Decide(string testSelectorQuery, bool inclusionMode, IReadOnlyDictionary<string, ITestSelector> testSelectors);
}