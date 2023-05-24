using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.TestsGenerators;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class SuppressPartOfTestsByIncludeFileTests : IClassFixture<DotnetContainerFixture>
{
    private readonly DotnetContainerFixture _fixture;

    public SuppressPartOfTestsByIncludeFileTests(DotnetContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Init(output);
    }

    [Theory]
    [InlineData(typeof(NUnitTestProjectGenerator))]
    [InlineData(typeof(XUnitTestProjectGenerator))]
    [InlineData(typeof(MsTestTestProjectGenerator))]
    public async Task Run(Type testProjectGeneratorType)
    {
        // arrange
        const string projectName = "MyTestProject";
        var testClass0 = new TestClassDescription("TestClass0", "Test0", "Test1", "Test2");
        var testClass1 = new TestClassDescription("TestClass1", "Test0", "Test1", "Test2", "Test3");
        var testClass2 = new TestClassDescription("TestClass2", "Test0", "Test1");
        var allTestClasses = new[] { testClass0, testClass1, testClass2 };
        var allTestsNames = allTestClasses.GetFullTestMethodsNames(projectName);
        var testClassesToInclude = new[] { testClass0, testClass2 };
        var testNamesToInclude = testClassesToInclude.GetFullTestMethodsNames(projectName);
        var testNamesToExclude = testClass1.GetFullTestMethodsNames(projectName);

        var (testQueriesFilePath, targetAssemblyPath) =
            await _fixture.CreateTestProject(testProjectGeneratorType, projectName, allTestClasses, testClassesToInclude);

        // act
        var (beforeTestOutput, beforeTestNamesExecuted) = await _fixture.RunTests(targetAssemblyPath);
        await _fixture.RunFilterApp($"suppress -t {targetAssemblyPath} -l {testQueriesFilePath} -i -v detailed"); // `-i` stands for "inclusion mode"
        var (afterTestOutput, afterTestNamesExecuted) = await _fixture.RunTests(targetAssemblyPath);

        // assert
        Assert.Equal(9, beforeTestNamesExecuted.Count);
        Assert.True(allTestsNames.ContainsSameElements(beforeTestNamesExecuted));
        Assert.Contains($"Passed!  - Failed:     0, Passed:     9, Skipped:     0, Total:     9", beforeTestOutput.Stdout);
        Assert.Equal(testNamesToInclude.Count, afterTestNamesExecuted.Count);
        Assert.True(testNamesToInclude.ContainsSameElements(afterTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(afterTestNamesExecuted).Any());
        Assert.Contains($"Passed!  - Failed:     0, Passed:     5, Skipped:     0, Total:     5", afterTestOutput.Stdout);
    }
}