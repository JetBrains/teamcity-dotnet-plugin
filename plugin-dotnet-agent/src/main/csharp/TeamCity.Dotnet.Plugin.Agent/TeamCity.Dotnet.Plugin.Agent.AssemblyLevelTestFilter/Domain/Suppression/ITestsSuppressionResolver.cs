namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal interface ITestsSuppressionResolver
{ 
    TestSuppressionCriteria ResolveCriteria(string testClassName);
}