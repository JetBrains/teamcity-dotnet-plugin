namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

internal class TestClassQuery : ITestsQuery
{
    public TestClassQuery(string suiteName, string className)
    {
        SuiteName = suiteName;
        ClassName = className;
    }

    public string SuiteName { get; }
    
    public string ClassName { get; }
    
    public string Query => $"{SuiteName}.{ClassName}";
}