namespace TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

internal record TestClassSelector(IList<string> Namespaces, string ClassName) : ITestSelector
{
    public string Query => $"{string.Join(".", Namespaces)}.{ClassName}";
}