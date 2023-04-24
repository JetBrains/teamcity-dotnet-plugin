namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal interface ITestsSelector
{
    string Query { get; }
}
