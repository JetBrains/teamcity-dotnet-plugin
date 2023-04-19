namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;

internal interface ITestsSuppressionResolver
{
    public TestSuppressionCriterion ResolveCriteria(string testClassName);
}