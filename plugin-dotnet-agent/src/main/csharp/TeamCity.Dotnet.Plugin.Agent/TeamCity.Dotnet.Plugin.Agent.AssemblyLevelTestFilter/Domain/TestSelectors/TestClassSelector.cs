namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal record TestClassSelector(IList<string> Namespaces, string ClassName) : ITestSelector
{
    public string Query => $"{string.Join(".", Namespaces)}.{ClassName}";
}