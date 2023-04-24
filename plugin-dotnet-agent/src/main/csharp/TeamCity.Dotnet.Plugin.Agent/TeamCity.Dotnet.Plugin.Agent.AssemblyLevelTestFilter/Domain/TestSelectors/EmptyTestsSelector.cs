namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal record EmptyTestsSelector : ITestsSelector
{
    public string Query => "<empty tests query>";
}