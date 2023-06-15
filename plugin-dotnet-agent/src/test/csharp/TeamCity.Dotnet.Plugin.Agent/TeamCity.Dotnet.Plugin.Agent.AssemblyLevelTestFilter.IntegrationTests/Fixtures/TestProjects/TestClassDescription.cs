namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;

public record TestClassDescription(string ClassName, params string[] TestMethodsNames);