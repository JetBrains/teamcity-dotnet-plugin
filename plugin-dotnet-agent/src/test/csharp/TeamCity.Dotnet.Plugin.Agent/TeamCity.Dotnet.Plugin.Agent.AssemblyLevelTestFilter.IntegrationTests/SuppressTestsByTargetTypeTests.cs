using DotNet.Testcontainers.Containers;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class SuppressTestsByTargetTypeTests : IClassFixture<DotnetTestContainerFixture>
{
    private readonly DotnetTestContainerFixture _fixture;

    public SuppressTestsByTargetTypeTests(DotnetTestContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Init(output);
    }

    [Theory]
    [InlineData(DotnetTestContainerFixture.TargetType.Assembly, DotnetVersion.v8_0_Preview)]
    [InlineData(DotnetTestContainerFixture.TargetType.Assembly, DotnetVersion.v7_0)]
    [InlineData(DotnetTestContainerFixture.TargetType.Assembly, DotnetVersion.v6_0)]
    [InlineData(DotnetTestContainerFixture.TargetType.Project, DotnetVersion.v8_0_Preview)]
    [InlineData(DotnetTestContainerFixture.TargetType.Project, DotnetVersion.v7_0)]
    [InlineData(DotnetTestContainerFixture.TargetType.Project, DotnetVersion.v6_0)]
    [InlineData(DotnetTestContainerFixture.TargetType.Solution, DotnetVersion.v8_0_Preview)]
    [InlineData(DotnetTestContainerFixture.TargetType.Solution, DotnetVersion.v7_0)]
    [InlineData(DotnetTestContainerFixture.TargetType.Solution, DotnetVersion.v6_0)]
    [InlineData(DotnetTestContainerFixture.TargetType.Directory, DotnetVersion.v8_0_Preview)]
    [InlineData(DotnetTestContainerFixture.TargetType.Directory, DotnetVersion.v7_0)]
    [InlineData(DotnetTestContainerFixture.TargetType.Directory, DotnetVersion.v6_0)]
    [InlineData(DotnetTestContainerFixture.TargetType.MsBuildBinLog, DotnetVersion.v8_0_Preview)]
    [InlineData(DotnetTestContainerFixture.TargetType.MsBuildBinLog, DotnetVersion.v7_0)]
    [InlineData(DotnetTestContainerFixture.TargetType.MsBuildBinLog, DotnetVersion.v6_0)]
    public async Task Run(DotnetTestContainerFixture.TargetType targetType, DotnetVersion dotnetVersion)
    {
        // arrange
        await _fixture.ReinitContainerWith(dotnetVersion);
        
        const string projectName = "MyTestProject";
        var testClass0 = new TestClassDescription("TestClass0", "Test0", "Test1", "Test2");
        var testClass1 = new TestClassDescription("TestClass1", "Test0", "Test1", "Test2", "Test3");
        var allTestClasses = new[] { testClass0, testClass1 };
        var allTestsNames = allTestClasses.GetFullTestMethodsNames(projectName);
        var testClassesToInclude = new[] { testClass0 };
        var testNamesToInclude = testClassesToInclude.GetFullTestMethodsNames(projectName);
        var testNamesToExclude = testClass1.GetFullTestMethodsNames(projectName);

        var (testQueriesFilePath, targetPath) = await _fixture.CreateTestProject(
            typeof(XUnitTestProject),   // it doesn't matter which project type we use here, because we are testing the target overriding
            dotnetVersion,
            projectName,
            withoutDebugSymbols: false,
            targetType,
            allTestClasses,
            buildTestProject: true,
            targetType == DotnetTestContainerFixture.TargetType.MsBuildBinLog,
            testClassesToInclude
        );

        // act
        var (beforeTestOutput, beforeTestNamesExecuted) = await _fixture.RunTests(targetPath);
        await _fixture.ExecAsync("ls -la ./test-project");
        await _fixture.RunFilterApp($"suppress -t {targetPath} -l {testQueriesFilePath} -i -v detailed"); // `-i` stands for "inclusion mode"
        var (afterTestOutput, afterTestNamesExecuted) = await _fixture.RunTests(targetPath);

        // assert
        Assert.Equal(7, beforeTestNamesExecuted.Count);
        Assert.True(allTestsNames.ContainsSameElements(beforeTestNamesExecuted));
        Assert.Contains($"Passed!  - Failed:     0, Passed:     7, Skipped:     0, Total:     7", beforeTestOutput.Stdout);
        Assert.Equal(testNamesToInclude.Count, afterTestNamesExecuted.Count);
        Assert.True(testNamesToInclude.ContainsSameElements(afterTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(afterTestNamesExecuted).Any());
        Assert.Contains($"Passed!  - Failed:     0, Passed:     3, Skipped:     0, Total:     3", afterTestOutput.Stdout);
    }
}