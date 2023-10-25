using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests;

public class SuppressTestsByInvalidBinlog : IClassFixture<DotnetTestContainerFixture>
{
    private readonly DotnetTestContainerFixture _fixture;

    public SuppressTestsByInvalidBinlog(DotnetTestContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Init(output);
    }
    
    [Theory]
    [InlineData(DotnetVersion.v8_0_Preview)]
    [InlineData(DotnetVersion.v7_0)]
    [InlineData(DotnetVersion.v6_0)]
    public async Task Run_Against_Invalid_Binlog(DotnetVersion dotnetVersion)
    {
        // arrange
        await _fixture.ReinitContainerWith(dotnetVersion);
        
        const string testQueriesFilePath = "/1.txt";
        await _fixture.ExecBashAsync($"echo 'Namespace0.Namespace1.ClassName' >> {testQueriesFilePath}");
        
        const string binlogTargetPath = "/1.binlog";
        await _fixture.ExecBashAsync($"echo 'INVALID' >> {binlogTargetPath}");

        // act
        var execResult = await _fixture.RunFilterApp($"suppress -t {binlogTargetPath} -l {testQueriesFilePath} -i");

        // assert
        Assert.Contains($"Target MSBuild .binlog {binlogTargetPath} is invalid", execResult.Stdout);
    }
}