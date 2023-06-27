using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class SuppressNotBuiltProjectTests : IClassFixture<DotnetTestContainerFixture>
{
    private readonly DotnetTestContainerFixture _fixture;

    public SuppressNotBuiltProjectTests(DotnetTestContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Init(output);
    }

    [Fact]
    public async Task Run()
    {
        const DotnetVersion dotnetVersion = DotnetVersion.v8_0_Preview;
        
        // arrange
        await _fixture.ReinitContainerWith(dotnetVersion);

        var testClass = new TestClassDescription("TestClass0", "Test0", "Test1", "Test2");

        var (testQueriesFilePath, targetPath) = await _fixture.CreateTestProject(
            typeof(XUnitTestProject),
            dotnetVersion,
            projectName: "MyTestProject",
            withoutDebugSymbols:false,
            DotnetTestContainerFixture.TargetType.Project,
            projectTestClasses: new[] { testClass },
            buildTestProject: false,  // in this test we a not going to build the project and see if the filter fails
            withMsBuildBinaryLogs: false,
            testClass
        );

        // act
        var execResult = await _fixture.RunFilterApp($"suppress -t {targetPath} -l {testQueriesFilePath} -v detailed");

        // assert
        Assert.Contains("Target project output file /app/test-project/MyTestProject.csproj does not exist", execResult.Stdout);
        Assert.Contains("Patching finished: 0 assemblies patched", execResult.Stdout);
    }
}

