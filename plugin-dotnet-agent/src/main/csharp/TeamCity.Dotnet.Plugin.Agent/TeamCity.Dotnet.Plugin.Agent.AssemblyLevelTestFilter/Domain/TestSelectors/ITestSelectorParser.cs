namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal interface ITestSelectorParser
{
    ITestsSelector? ParseTestQuery(string value);
}