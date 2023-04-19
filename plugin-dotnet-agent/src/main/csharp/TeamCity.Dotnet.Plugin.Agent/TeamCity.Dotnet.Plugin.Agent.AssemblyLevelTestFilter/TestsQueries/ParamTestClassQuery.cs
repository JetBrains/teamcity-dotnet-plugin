namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

internal class ParamTestClassQuery : ITestsQuery
{
    public ParamTestClassQuery(string suiteName, string className, IList<string> parameters)
    {
        SuiteName = suiteName;
        ClassName = className;
        Parameters = parameters;
    }

    public string SuiteName { get; }

    public string ClassName { get; }

    public IList<string> Parameters { get; }

    public string Query => $"{SuiteName}.{ClassName}({string.Join(",", Parameters)})";
}
