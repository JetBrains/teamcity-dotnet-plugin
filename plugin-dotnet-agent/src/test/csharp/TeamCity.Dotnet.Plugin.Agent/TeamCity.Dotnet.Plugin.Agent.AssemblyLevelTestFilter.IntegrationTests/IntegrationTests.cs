using System.Diagnostics;
using DotNet.Testcontainers.Builders;
using DotNet.Testcontainers.Configurations;
using DotNet.Testcontainers.Containers;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class IntegrationTests : IDisposable
{
    private const string HostTestProjectPath = "./test-project";
    private const string WorkDirPath = "/app";
    private const string MountedTestProjectSourcesDirPath = "/test-project-sources";
    private const string MountedFilterAppSourcesDirPath = "/filter-app-sources";
    private const string TestProjectSourcesDirPath = WorkDirPath + "/test-project";
    private const string FilterAppDirPath = WorkDirPath + "/filter-app";
    private const string FilterAppSourcesDirPath = FilterAppDirPath + "/sources";
    private const string FilterAppName = "TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter";
    private const string FilterAppPath = FilterAppDirPath + "/" + FilterAppName;
    private readonly ITestOutputHelper _output;
    private readonly IContainer _container;

    public IntegrationTests(ITestOutputHelper output)
    {
        _output = output;
        Directory.CreateDirectory(HostTestProjectPath);
        _container = new ContainerBuilder()
            .WithImage("mcr.microsoft.com/dotnet/sdk:8.0-preview")
            .WithName("tc-dotnet-plugin-agent-tests-filter__integration-tests")
            .WithWorkingDirectory(WorkDirPath)
            .WithCommand("tail", "-f", "/dev/null")
            .WithBindMount($"{Directory.GetCurrentDirectory()}/test-project", MountedTestProjectSourcesDirPath, AccessMode.ReadOnly)
            .WithBindMount($"{Directory.GetCurrentDirectory()}/sources", MountedFilterAppSourcesDirPath, AccessMode.ReadOnly)
            .Build();
        _container.StartAsync().Wait();
        Prepare().Wait();
    }

    [Fact]
    public async Task HappyPath_OneTestRemoved_NoTestsRun()
    {
        // arrange
        const string projectName = "MyNUnitTestProject";
        const string projectPath = WorkDirPath + "/" + projectName;
        await ExecAsync($"dotnet new nunit -n {projectName}");
        await ExecAsync($"dotnet build {projectPath}");
        const string targetAssemblyPath = $"{projectPath}/bin/Debug/net8.0/{projectName}.dll";
        const string excludeFileName = "exclude.txt";
        const string excludeFilePath = TestProjectSourcesDirPath + "/" + excludeFileName;
        await File.WriteAllTextAsync($"./test-project/{excludeFileName}", $"{projectName}.Tests");
        await ExecAsync($"cp -a {MountedTestProjectSourcesDirPath}/. {TestProjectSourcesDirPath}");
        
        
        // act
        var before = await ExecAsync($"dotnet test {targetAssemblyPath}");
        await ExecAsync($"{FilterAppPath} suppress -t {targetAssemblyPath} -l {excludeFilePath}");
        var after = await ExecAsync($"dotnet test {targetAssemblyPath}");
        
        // assert
        Assert.Contains("Passed!  - Failed:     0, Passed:     1, Skipped:     0, Total:     1", before.Stdout);
        Assert.Contains("No test is available in", after.Stdout);
    }

    private async Task Prepare()
    {
        // filter app
        await ExecAsync($"mkdir {FilterAppDirPath}");
        await ExecAsync($"mkdir {FilterAppSourcesDirPath}");
        await ExecAsync($"cp -a {MountedFilterAppSourcesDirPath}/. {FilterAppSourcesDirPath}");
        await ExecAsync($"dotnet publish {FilterAppSourcesDirPath} -c Release -o {FilterAppDirPath} --verbosity quiet");
        
        // test project
        await ExecAsync($"mkdir {TestProjectSourcesDirPath}");
        await ExecAsync($"cp -a {MountedTestProjectSourcesDirPath}/. {TestProjectSourcesDirPath}");
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
        DeleteDirectoryContents(HostTestProjectPath);
    }
    
    private static void DeleteDirectoryContents(string directoryPath)
    {
        foreach (var file in Directory.GetFiles(directoryPath))
        {
            File.Delete(file);
        }
        foreach (var subdirectory in Directory.GetDirectories(directoryPath))
        {
            DeleteDirectoryContents(subdirectory);
        }
    }
}
