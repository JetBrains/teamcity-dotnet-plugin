using System.Diagnostics;
using DotNet.Testcontainers.Builders;
using DotNet.Testcontainers.Configurations;
using DotNet.Testcontainers.Containers;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class MyIntegrationTests : IDisposable
{
    private const string WorkDirPath = "/app";
    private const string FilterAppDirPath = "/app/filter-app";
    private const string FilterAppPath = FilterAppDirPath + "/TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.dll";
    private readonly ITestOutputHelper _output;
    private readonly IContainer _container;

    public MyIntegrationTests(ITestOutputHelper output)
    {
        _output = output;
        _container = new ContainerBuilder()
            .WithImage("mcr.microsoft.com/dotnet/sdk:7.0")
            .WithName("tc-dotnet-plugin-agent-tests-filter__integration-tests")
            .WithWorkingDirectory(WorkDirPath)
            .WithCommand("tail", "-f", "/dev/null")
            .WithBindMount(Directory.GetCurrentDirectory(), FilterAppDirPath, AccessMode.ReadOnly)
            .Build();
        _container.StartAsync().Wait();
    }

    [Fact]
    public async Task BuildAndRunTests()
    {
        const string projectName = "MyNUnitTestProject";
        const string projectPath = "/app/" + projectName;
        await ExecAsync($"dotnet new nunit -n {projectName}");
        await ExecAsync($"dotnet test {projectPath}");
    }


    private async Task<ExecResult> ExecAsync(string command)
    {
        string Format(string value, string linePrefix = "  ")
        {
            var lines = value.Trim().Split('\n').Select(x => linePrefix + x);
            return string.Join('\n', lines);
        }

        _output.WriteLine("> " + command);
        var watch = Stopwatch.StartNew();
        var result = await _container.ExecAsync(command.Split(" "));
        watch.Stop();
        if (result.ExitCode != 0) _output.WriteLine("Exit code: " + result.ExitCode);
        if (!string.IsNullOrWhiteSpace(result.Stdout)) _output.WriteLine(Format(result.Stdout));
        if (!string.IsNullOrWhiteSpace(result.Stderr)) _output.WriteLine(Format(result.Stderr, "[ERROR]  "));
        var elapsedTime =
            $"{watch.Elapsed.Hours:00}:{watch.Elapsed.Minutes:00}:{watch.Elapsed.Seconds:00}.{watch.Elapsed.Milliseconds:000}";
        _output.WriteLine($"⏱ Elapsed: {elapsedTime}");
        return result;
    }

    public void Dispose()
    {
        _container.DisposeAsync().AsTask().Wait();
    }
    
    
}
