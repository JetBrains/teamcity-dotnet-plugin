using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Extensions;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests;

public class RestoreTests : IClassFixture<DotnetTestContainerFixture>
{
    private readonly DotnetTestContainerFixture _fixture;

    public RestoreTests(DotnetTestContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Init(output);
    }

    [Theory]
    [InlineData(false)]
    [InlineData(true)]
    public async Task Run(bool withoutDebugSymbols)
    {
        const DotnetVersion dotnetVersion = DotnetVersion.v8_0_Preview;
        
        // arrange
        await _fixture.ReinitContainerWith(dotnetVersion);
        
        const string projectName = "MyTestProject";
        var testClass0 = new TestClassDescription("TestClass0", "Test0", "Test1", "Test2");
        var testClass1 = new TestClassDescription("TestClass1", "Test0", "Test1", "Test2", "Test3");
        var allTestClasses = new[] { testClass0, testClass1 };
        var allTestsNames = allTestClasses.GetFullTestMethodsNames(projectName);
        var testClassesToExclude = new[] { testClass0 };
        var testNamesToExclude = testClassesToExclude.GetFullTestMethodsNames(projectName);
        var testNamesToInclude = testClass1.GetFullTestMethodsNames(projectName);
        const string backupFileName = "backup-metadata.csv";

        var (testQueriesFilePath, targetPath) = await _fixture.CreateTestProject(
            typeof(XUnitTestProject),
            new [] { dotnetVersion },
            projectName,
            withoutDebugSymbols,
            CommandTargetType.Project,
            allTestClasses,
            buildTestProject: true,
            withMsBuildBinaryLogs: false,
            testClassesToExclude
        );

        // act
        var (_, beforeTestNamesExecuted) = await _fixture.RunTests(targetPath);
        await _fixture.RunFilterApp($"suppress -t {targetPath} -l {testQueriesFilePath} -b {backupFileName} -v detailed");
        var (_, afterFirstSuppressTestNamesExecuted) = await _fixture.RunTests(targetPath);
        await _fixture.RunFilterApp($"restore -b {backupFileName} -v detailed");
        var (_, afterFirstRestoreTestNamesExecuted) = await _fixture.RunTests(targetPath);
        await _fixture.RunFilterApp($"suppress -t {targetPath} -l {testQueriesFilePath} -b {backupFileName} -v detailed");
        var (_, afterSecondSuppressTestNamesExecuted) = await _fixture.RunTests(targetPath);
        await _fixture.RunFilterApp($"restore -b {backupFileName} -v detailed");
        var (_, afterSecondRestoreTestNamesExecuted) = await _fixture.RunTests(targetPath);

        // assert
        // 0. before first suppression
        Assert.Equal(allTestsNames.Count, beforeTestNamesExecuted.Count);
        Assert.True(allTestsNames.ContainsSameElements(beforeTestNamesExecuted));
        // 1. after first suppression
        Assert.Equal(testNamesToInclude.Count, afterFirstSuppressTestNamesExecuted.Count);
        Assert.True(testNamesToInclude.ContainsSameElements(afterFirstSuppressTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(afterFirstSuppressTestNamesExecuted).Any());
        // 2. after first restore
        Assert.Equal(allTestsNames.Count, afterFirstRestoreTestNamesExecuted.Count);
        Assert.True(allTestsNames.ContainsSameElements(afterFirstRestoreTestNamesExecuted));
        // 3. after second suppression
        Assert.Equal(testNamesToInclude.Count, afterSecondSuppressTestNamesExecuted.Count);
        Assert.True(testNamesToInclude.ContainsSameElements(afterSecondSuppressTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(afterSecondSuppressTestNamesExecuted).Any());
        // 4. after second restore
        Assert.Equal(allTestsNames.Count, afterSecondRestoreTestNamesExecuted.Count);
        Assert.True(allTestsNames.ContainsSameElements(afterSecondRestoreTestNamesExecuted));
    }
}

