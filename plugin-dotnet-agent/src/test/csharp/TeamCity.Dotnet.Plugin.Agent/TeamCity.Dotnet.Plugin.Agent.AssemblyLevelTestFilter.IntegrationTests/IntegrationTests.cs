using System.Diagnostics;
using System.Xml;
using System.Xml.Linq;
using DotNet.Testcontainers.Builders;
using DotNet.Testcontainers.Configurations;
using DotNet.Testcontainers.Containers;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class IntegrationTests : IDisposable
{
    private readonly string _hostTestProjectPath = $"{Directory.GetCurrentDirectory()}/test-project";
    private readonly string _hostFilterAppSourcesPath = $"{Directory.GetCurrentDirectory()}/sources";
    private const string MountedTestProjectSourcesDirPath = "/test-project-sources";
    private const string MountedFilterAppSourcesDirPath = "/filter-app-sources";
    private const string WorkDirPath = "/app";
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
        Directory.CreateDirectory(_hostTestProjectPath);
        _container = new ContainerBuilder()
            .WithImage("mcr.microsoft.com/dotnet/sdk:8.0-preview")
            .WithName("tc-dotnet-plugin-agent-tests-filter__integration-tests")
            .WithWorkingDirectory(WorkDirPath)
            .WithCommand("tail", "-f", "/dev/null")
            .WithBindMount(_hostTestProjectPath, MountedTestProjectSourcesDirPath, AccessMode.ReadOnly)
            .WithBindMount(_hostFilterAppSourcesPath, MountedFilterAppSourcesDirPath, AccessMode.ReadOnly)
            .Build();
        _container.StartAsync().Wait();
        Prepare().Wait();
    }

    [Fact]
    public async Task HappyPath_OneTestRemoved_NoTestsRun()
    {
        // arrange
        const string projectName = "MyNUnitTestProject";
        var testProjectGenerator = new NUnitTestProjectGenerator();
        await testProjectGenerator.GenerateAsync(_hostTestProjectPath, projectName);
        const string testsQueriesFileName = "exclude.txt";
        const string testsQueriesFilePath = $"{TestProjectSourcesDirPath}/{testsQueriesFileName}";
        await File.WriteAllTextAsync($"{_hostTestProjectPath}/{testsQueriesFileName}", $"{projectName}.Tests");
        await ExecAsync($"cp -a {MountedTestProjectSourcesDirPath}/. {TestProjectSourcesDirPath}");
        await ExecAsync($"dotnet build {TestProjectSourcesDirPath}");
        const string targetAssemblyPath = $"{TestProjectSourcesDirPath}/bin/Debug/net8.0/{projectName}.dll";

        // act
        const string beforeTrxReportFilePath = $"{TestProjectSourcesDirPath}/before-report.trx";
        var beforeTestOutput = await ExecAsync($"dotnet test {targetAssemblyPath} -l:trx;LogFileName={beforeTrxReportFilePath}");
        var beforeTestNames = await GetTestNames(beforeTrxReportFilePath);
        
        await ExecAsync($"{FilterAppPath} suppress -t {targetAssemblyPath} -l {testsQueriesFilePath}");
        
        const string afterTrxReportFilePath = $"{TestProjectSourcesDirPath}/after-report.trx";
        var afterTestOutput = await ExecAsync($"dotnet test {targetAssemblyPath} -l:trx;LogFileName={afterTrxReportFilePath}");
        var afterTestNames = await GetTestNames(afterTrxReportFilePath);
        
        // assert
        Assert.Contains("Passed!  - Failed:     0, Passed:     1, Skipped:     0, Total:     1", beforeTestOutput.Stdout);
        Assert.Contains("No test is available in", afterTestOutput.Stdout);
        Assert.Equal(1, beforeTestNames.Count);
        Assert.Empty(afterTestNames);
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
    
    private async Task<IReadOnlyList<string>> GetTestNames(string trxReportFilePath)
    {
        var execResult = await ExecAsync($"cat {trxReportFilePath}", silent: true);
        var doc = XDocument.Parse(execResult.Stdout);
        XNamespace ns = "http://microsoft.com/schemas/VisualStudio/TeamTest/2010";
        return doc.Descendants(ns + "UnitTest")
            .Select(test => test.Element(ns + "TestMethod"))
            .Where(method => method != null)
            .Select(method =>
            {
                var className = method!.Attribute("className")?.Value;
                var methodName = method.Attribute("name")?.Value;
                return $"{className}.{methodName}";
            })
            .ToList();
    }

    private async Task<ExecResult> ExecAsync(string command, bool silent = false)
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
        if (silent)
        {
            return result;
        }
        
        if (result.ExitCode != 0) _output.WriteLine("Exit code: " + result.ExitCode);
        if (!string.IsNullOrWhiteSpace(result.Stdout)&& !silent) _output.WriteLine(Format(result.Stdout));
        if (!string.IsNullOrWhiteSpace(result.Stderr)) _output.WriteLine(Format(result.Stderr, "[ERROR]  "));
        var elapsedTime =
            $"{watch.Elapsed.Hours:00}:{watch.Elapsed.Minutes:00}:{watch.Elapsed.Seconds:00}.{watch.Elapsed.Milliseconds:000}";
        _output.WriteLine($"⏱ Elapsed: {elapsedTime}");
        return result;
    }

    public void Dispose()
    {
        _container.DisposeAsync().AsTask().Wait();

        DeleteDirectoryContents(_hostTestProjectPath);
        Directory.Delete(_hostTestProjectPath);
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
