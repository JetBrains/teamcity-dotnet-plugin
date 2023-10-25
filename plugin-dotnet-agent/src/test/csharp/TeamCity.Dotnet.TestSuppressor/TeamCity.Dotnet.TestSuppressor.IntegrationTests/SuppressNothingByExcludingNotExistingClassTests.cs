using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Extensions;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests;

[Collection(".NET containers")]
public class SuppressNothingByExcludingNotExistingClassTests
{
    private readonly DotnetTestContainerFixture _fixture;

    public SuppressNothingByExcludingNotExistingClassTests(DotnetTestContainerFixture fixture, ITestOutputHelper output)
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
        var testClassesToInclude = new[] { testClass0, testClass1 };
        var testNamesToInclude = testClassesToInclude.GetFullTestMethodsNames(projectName);
        var testNamesToExclude = testClass2.GetFullTestMethodsNames(projectName);

        var testProjectData = await _fixture.CreateTestProject(
            testProjectGeneratorType,
            new [] { dotnetVersion },
            projectName,
            withoutDebugSymbols: false,
            CommandTargetType.Assembly,
            testClassesToInclude,
            buildTestProject: true,
            withMsBuildBinaryLog: false,
            testClass2
        );

        // act
        var (beforeTestOutput, beforeTestNamesExecuted) = await _fixture.RunTests(testProjectData.TargetPath);
        await _fixture.RunFilterApp($"suppress -t {testProjectData.TargetPath} -l {testProjectData.QueriesFilePath} -v detailed");
        var (afterTestOutput, afterTestNamesExecuted) = await _fixture.RunTests(testProjectData.TargetPath);

        // assert
        Assert.Equal(7, beforeTestNamesExecuted.Count);
        Assert.Contains(
            expectedSubstring: "Passed!  - Failed:     0, Passed:     7, Skipped:     0, Total:     7",
            actualString: beforeTestOutput.Stdout
        );
        Assert.Equal(7, afterTestNamesExecuted.Count);
        Assert.Contains(
            expectedSubstring: "Passed!  - Failed:     0, Passed:     7, Skipped:     0, Total:     7",
            actualString: afterTestOutput.Stdout
        );
        Assert.True(testNamesToInclude.ContainsSameElements(beforeTestNamesExecuted));
        Assert.True(beforeTestNamesExecuted.ContainsSameElements(afterTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(beforeTestNamesExecuted).Any());
        Assert.False(testNamesToExclude.Intersect(afterTestNamesExecuted).Any());
    }
}