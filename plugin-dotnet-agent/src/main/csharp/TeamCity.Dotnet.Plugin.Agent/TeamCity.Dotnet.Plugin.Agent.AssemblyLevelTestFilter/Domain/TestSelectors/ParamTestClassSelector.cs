namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal record ParamTestClassSelector(IList<string> Namespaces, string ClassName, IList<string> Parameters) : ITestSelector
{
    public string Query => $"{string.Join(".", Namespaces)}.{ClassName}({string.Join(",", Parameters)})";
}
