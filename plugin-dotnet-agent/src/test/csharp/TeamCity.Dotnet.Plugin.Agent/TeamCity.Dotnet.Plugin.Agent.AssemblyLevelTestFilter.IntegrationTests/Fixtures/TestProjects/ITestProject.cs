using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;

internal interface ITestProject
{
    ITestEngine TestEngine { get; }
    
    Task GenerateAsync(
        DotnetVersion dotnetVersion,
        string directoryPath,
        string projectName,
        bool withSolution,
        params TestClassDescription[] testClasses
    );
}