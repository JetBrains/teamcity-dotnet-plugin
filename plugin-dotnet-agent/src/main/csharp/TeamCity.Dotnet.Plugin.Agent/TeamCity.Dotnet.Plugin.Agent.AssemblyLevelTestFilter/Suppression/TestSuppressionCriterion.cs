using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;

internal struct TestSuppressionCriterion
{
    public TestSuppressionCriterion(bool shouldBeSuppressed, ITestsQuery testsQuery)
    {
        ShouldBeSuppressed = shouldBeSuppressed;
        TestsQuery = testsQuery;
    }
    
    public bool ShouldBeSuppressed { get; }
    
    public ITestsQuery TestsQuery { get; }
}