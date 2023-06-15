using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;

public static class TestClassDescriptionExtension
{
    public static IReadOnlyList<string> GetFullTestMethodsNames(this TestClassDescription[] testClassDescriptions, string projectName) =>
        testClassDescriptions.SelectMany(d => d.TestMethodsNames.Select(m => $"{projectName}.{d.ClassName}.{m}")).ToList();
    
    public static IReadOnlyList<string> GetFullTestMethodsNames(this TestClassDescription testClassDescription, string projectName) =>
        testClassDescription.TestMethodsNames.Select(m => $"{projectName}.{testClassDescription.ClassName}.{m}").ToList();
}