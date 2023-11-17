namespace TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

internal record TestSelector(IList<string> Namespaces, string ClassName)
{
    public string Query => string.Join(".", Namespaces.Append(ClassName));
}