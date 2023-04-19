namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

internal interface ITestQueryParser
{
    ITestsQuery? ParseTestQuery(string value);
}