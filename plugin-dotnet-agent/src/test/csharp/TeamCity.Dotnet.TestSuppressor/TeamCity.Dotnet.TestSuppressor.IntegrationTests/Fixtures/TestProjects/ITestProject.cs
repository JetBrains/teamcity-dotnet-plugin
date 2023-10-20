using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;

internal interface ITestProject
{
    ITestEngine TestEngine { get; }
    
    Task GenerateAsync(TestProjectSettings settings, params TestClassDescription[] testClasses);
}