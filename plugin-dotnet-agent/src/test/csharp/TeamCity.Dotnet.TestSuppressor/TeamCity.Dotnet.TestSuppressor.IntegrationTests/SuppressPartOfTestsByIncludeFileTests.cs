using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Extensions;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests;

public class SuppressPartOfTestsByIncludeFileTests : IClassFixture<DotnetTestContainerFixture>
{
    private readonly DotnetTestContainerFixture _fixture;

    public SuppressPartOfTestsByIncludeFileTests(DotnetTestContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Init(output);
    }

    [Theory]
    [InlineData(typeof(XUnitTestProject), DotnetVersion.v8_0_Preview)]
    [InlineData(typeof(XUnitTestProject), DotnetVersion.v7_0)]
    [InlineData(typeof(XUnitTestProject), DotnetVersion.v6_0)]
    [InlineData(typeof(NUnitTestProject), DotnetVersion.v8_0_Preview)]
    [InlineData(typeof(NUnitTestProject), DotnetVersion.v7_0)]
    [InlineData(typeof(NUnitTestProject), DotnetVersion.v6_0)]
    [InlineData(typeof(MsTestTestProject), DotnetVersion.v8_0_Preview)]
    [InlineData(typeof(MsTestTestProject), DotnetVersion.v7_0)]
    [InlineData(typeof(MsTestTestProject), DotnetVersion.v6_0)]
    public async Task Run(Type testProjectGeneratorType, DotnetVersion dotnetVersion)
    {
        // arrange
        await _fixture.ReinitContainerWith(dotnetVersion);
        
        const string projectName = "MyTestProject";
        var testClass0 = new TestClassDescription("TestClass0", "Test0", "Test1", "Test2");
        var testClass1 = new TestClassDescription("TestClass1", "Test0", "Test1", "Test2", "Test3");
        var testClass2 = new TestClassDescription("TestClass2", "Test0", "Test1");
        var allTestClasses = new[] { testClass0, testClass1, testClass2 };
        var allTestsNames = allTestClasses.GetFullTestMethodsNames(projectName);
        var testClassesToInclude = new[] { testClass0, testClass2 };
        var testNamesToInclude = testClassesToInclude.GetFullTestMethodsNames(projectName);
        var testNamesToExclude = testClass1.GetFullTestMethodsNames(projectName);

        var testProjectData = await _fixture.CreateTestProject(
            testProjectGeneratorType,
            new [] { dotnetVersion },
            projectName,
            withoutDebugSymbols: false,
            CommandTargetType.Assembly,
            allTestClasses,
            buildTestProject: true,
            withMsBuildBinaryLog: false,
            testClassesToInclude
        );

        // act
        var (beforeTestOutput, beforeTestNamesExecuted) = await _fixture.RunTests(testProjectData.TargetPath);
        await _fixture.RunFilterApp($"suppress -t {testProjectData.TargetPath} -l {testProjectData.QueriesFilePath} -i -v detailed"); // `-i` stands for "inclusion mode"
        var (afterTestOutput, afterTestNamesExecuted) = await _fixture.RunTests(testProjectData.TargetPath);

        // assert
        Assert.Equal(9, beforeTestNamesExecuted.Count);
        Assert.True(allTestsNames.ContainsSameElements(beforeTestNamesExecuted));
        Assert.Contains(
            expectedSubstring: "Passed!  - Failed:     0, Passed:     9, Skipped:     0, Total:     9",
            actualString: beforeTestOutput.Stdout
        );
        Assert.Equal(testNamesToInclude.Count, afterTestNamesExecuted.Count);
        Assert.True(testNamesToInclude.ContainsSameElements(afterTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(afterTestNamesExecuted).Any());
        Assert.Contains(
            expectedSubstring: "Passed!  - Failed:     0, Passed:     5, Skipped:     0, Total:     5",
            actualString: afterTestOutput.Stdout
        );
    }
}