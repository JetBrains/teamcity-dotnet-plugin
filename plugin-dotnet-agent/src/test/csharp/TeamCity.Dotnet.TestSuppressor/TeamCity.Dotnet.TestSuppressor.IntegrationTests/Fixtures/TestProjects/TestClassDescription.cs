namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;

public record TestClassDescription(string ClassName, params string[] TestMethodsNames);