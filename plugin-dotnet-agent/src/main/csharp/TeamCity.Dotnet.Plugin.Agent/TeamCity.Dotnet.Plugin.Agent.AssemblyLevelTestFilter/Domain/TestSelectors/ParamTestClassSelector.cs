namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal record ParamTestClassSelector(string SuiteName, string ClassName, IList<string> Parameters) : ITestsSelector
{
    public string Query => $"{SuiteName}.{ClassName}({string.Join(",", Parameters)})";
}
