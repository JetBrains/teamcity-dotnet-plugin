using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Extensions;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests;

public class SuppressTestsByTargetTypeTests : IClassFixture<DotnetTestContainerFixture>
{
    private readonly DotnetTestContainerFixture _fixture;

    public SuppressTestsByTargetTypeTests(DotnetTestContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Init(output);
    }

    [Theory]
    [InlineData(CommandTargetType.Assembly, DotnetVersion.v8_0_Preview)]
    [InlineData(CommandTargetType.Assembly, DotnetVersion.v7_0)]
    [InlineData(CommandTargetType.Assembly, DotnetVersion.v6_0)]
    [InlineData(CommandTargetType.Project, DotnetVersion.v8_0_Preview)]
    [InlineData(CommandTargetType.Project, DotnetVersion.v7_0)]
    [InlineData(CommandTargetType.Project, DotnetVersion.v6_0)]
    [InlineData(CommandTargetType.Solution, DotnetVersion.v8_0_Preview)]
    [InlineData(CommandTargetType.Solution, DotnetVersion.v7_0)]
    [InlineData(CommandTargetType.Solution, DotnetVersion.v6_0)]
    [InlineData(CommandTargetType.Directory, DotnetVersion.v8_0_Preview)]
    [InlineData(CommandTargetType.Directory, DotnetVersion.v7_0)]
    [InlineData(CommandTargetType.Directory, DotnetVersion.v6_0)]
    [InlineData(CommandTargetType.MsBuildBinLog, DotnetVersion.v8_0_Preview)]
    [InlineData(CommandTargetType.MsBuildBinLog, DotnetVersion.v7_0)]
    [InlineData(CommandTargetType.MsBuildBinLog, DotnetVersion.v6_0)]
    
    // The following test cases check heuristics of parsing binary log,
    // which is generated when multiple target frameworks are used in one project
    [InlineData(
        CommandTargetType.MsBuildBinLog,
        DotnetVersion.v8_0_Preview, 
        new [] { DotnetVersion.v8_0_Preview, DotnetVersion.v7_0 }
    )]
    [InlineData(
        CommandTargetType.MsBuildBinLog,
        DotnetVersion.v7_0, 
        new [] { DotnetVersion.v7_0, DotnetVersion.v6_0 }
    )]
    public async Task Run(CommandTargetType commandTargetType, DotnetVersion dotnetVersion, DotnetVersion[]? targetFrameworks = null)
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

        // take container's dotnet runtime version as a target framework, if it's not provided
        targetFrameworks = targetFrameworks == null
            ? new [] { dotnetVersion }
            : targetFrameworks.DefaultIfEmpty(dotnetVersion).ToArray();

        var (testQueriesFilePath, targetPath) = await _fixture.CreateTestProject(
            typeof(XUnitTestProject),   // it doesn't matter which project type we use here, because we are testing the target overriding
            targetFrameworks,
            projectName,
            withoutDebugSymbols: false,
            commandTargetType,
            allTestClasses,
            buildTestProject: true,
            commandTargetType == CommandTargetType.MsBuildBinLog,
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
        Assert.Contains(
            expectedSubstring: "Passed!  - Failed:     0, Passed:     7, Skipped:     0, Total:     7",
            actualString: beforeTestOutput.Stdout
        );
        Assert.Equal(testNamesToInclude.Count, afterTestNamesExecuted.Count);
        Assert.True(testNamesToInclude.ContainsSameElements(afterTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(afterTestNamesExecuted).Any());
        Assert.Contains(
            expectedSubstring: "Passed!  - Failed:     0, Passed:     3, Skipped:     0, Total:     3",
            actualString: afterTestOutput.Stdout
        );
    }
}