namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal interface ITestSelectorParser
{
    bool TryParseTestQuery(string testQuery, out ITestSelector? testSelector);
}