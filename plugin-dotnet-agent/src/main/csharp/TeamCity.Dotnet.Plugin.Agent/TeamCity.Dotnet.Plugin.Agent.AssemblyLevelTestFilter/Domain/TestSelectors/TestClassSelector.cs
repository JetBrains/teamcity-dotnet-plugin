namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal record TestClassSelector(string SuiteName, string ClassName) : ITestsSelector
{
    public string Query => $"{SuiteName}.{ClassName}";
}