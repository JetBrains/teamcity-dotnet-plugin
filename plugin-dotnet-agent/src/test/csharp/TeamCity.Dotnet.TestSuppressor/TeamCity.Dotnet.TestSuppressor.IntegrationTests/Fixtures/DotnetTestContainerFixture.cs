using System.Collections.Concurrent;
using System.Diagnostics;
using System.Xml.Linq;
using DotNet.Testcontainers.Containers;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Extensions;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;

public partial class DotnetTestContainerFixture : IDisposable
{
    private readonly ConcurrentDictionary<DotnetVersion, DotnetTestSetup> _testSetup = new();
    private DotnetVersion _dotnetVersion = DotnetVersion.v8_0_Preview;
    
    private IContainer Container => _testSetup[_dotnetVersion].Container;
    
    private DotnetTestSetup TestSetup  => _testSetup[_dotnetVersion];

    public ITestOutputHelper Output { get; private set; } = null!;

    public void Init(ITestOutputHelper output)
    {
        Output = output;
        Output.WriteLine("Initializing test setup...");
    }

    public async Task ReinitContainerWith(DotnetVersion dotnetVersion)
    {
        _dotnetVersion = dotnetVersion;
        if (!_testSetup.ContainsKey(dotnetVersion))
        {
            var testSetup = new DotnetTestSetup(dotnetVersion);
            _testSetup.TryAdd(dotnetVersion, testSetup);
        }
        await Prepare().ConfigureAwait(false);
    }

    public async Task<ExecResult> ExecBashAsync(string command, bool silent = false)
    {
        var commandSegments = new [] { "/bin/bash", "-c", command };
        
        Output.WriteLine("> " + string.Join(' ', commandSegments));
        
        var result = await ExecCommandAsync(commandSegments, silent);
        if (result.ExitCode != 0)
        {
            throw new Exception("Invalid bash command result");
        }
        
        return result;
    }

    public Task<ExecResult> ExecAsync(string command, bool silent = false)
    {
        Output.WriteLine("> " + command);
        return ExecCommandAsync(command.Split(' '), silent);
    }

    private async Task<ExecResult> ExecCommandAsync(IList<string> commandSegments, bool silent)
    {
        string Format(string value, string linePrefix = "  ")
        {
            var lines = value.Trim().Split('\n').Select(x => linePrefix + x);
            return string.Join('\n', lines);
        }

        var watch = Stopwatch.StartNew();
        var result = await Container.ExecAsync(commandSegments);
        watch.Stop();
        if (silent)
        {
            return result;
        }
        
        if (result.ExitCode != 0) Output.WriteLine("Exit code: " + result.ExitCode);
        if (!string.IsNullOrWhiteSpace(result.Stdout)&& !silent) Output.WriteLine(Format(result.Stdout));
        if (!string.IsNullOrWhiteSpace(result.Stderr)) Output.WriteLine(Format(result.Stderr, "[ERROR]  "));
        var elapsedTime =
            $"{watch.Elapsed.Hours:00}:{watch.Elapsed.Minutes:00}:{watch.Elapsed.Seconds:00}.{watch.Elapsed.Milliseconds:000}";
        Output.WriteLine($"⏱ Elapsed: {elapsedTime}");
        return result;
    }

    public async Task<ParallelTestRunTarget> CreateTestProject(
        Type testProjectType,
        DotnetVersion[] targetFrameworks,
        string projectName,
        bool withoutDebugSymbols,
        CommandTargetType targetType,
        TestClassDescription[] projectTestClasses,
        bool buildTestProject = true,
        bool withMsBuildBinaryLog = false,
        params TestClassDescription[] testQueriesFileTestClasses
    ) {
        // generate test project
        var testProject = (ITestProject) Activator.CreateInstance(testProjectType)!;
        var withSolution = targetType == CommandTargetType.Solution;
        await testProject.GenerateAsync(
            settings: new TestProjectSettings(targetFrameworks.ToHashSet(), TestSetup.HostTestProjectPath, projectName, withSolution),
            testClasses: projectTestClasses
        );

        await ExecAsync($"cat {DotnetTestSetup.TestProjectSourcesDirPath}/{projectName}.csproj");
        
        // generate tests queries file
        var content = string.Join('\n', testQueriesFileTestClasses.Select(tc => $"{projectName}.{tc.ClassName}"));
        await File.WriteAllTextAsync(TestSetup.HostTestsQueriesFilePath, content);
        
        // copy test project and tests queries file to container
        await ExecAsync($"cp -a {DotnetTestSetup.MountedTestProjectSourcesDirPath}/. {DotnetTestSetup.TestProjectSourcesDirPath}");
        
        var binlogPath = $"{DotnetTestSetup.TestProjectSourcesDirPath}/msbuild.binlog";
        
        if (buildTestProject)
        {
            // build test project
            var debugSymbolsArg = withoutDebugSymbols ? "-p:DebugType=None -p:DebugSymbols=false" : "";
            var binlogArg = withMsBuildBinaryLog ? $"/bl:{binlogPath}" : "";
            var testProjectBuildResult =
                await ExecAsync($"dotnet build {DotnetTestSetup.TestProjectSourcesDirPath} {debugSymbolsArg} {binlogArg}");
            if (testProjectBuildResult.ExitCode != 0)
            {
                throw new Exception("Failed to build test project:\n" + testProjectBuildResult.Stderr);
            }
        }

        // we don't need all of the .dlls by target frameworks, only single one
        var targetFrameworkMoniker = targetFrameworks.First().GetMoniker();

        var targetPath = targetType switch
        {
            CommandTargetType.MsBuildBinLog => DotnetTestSetup.TestProjectSourcesDirPath,
            CommandTargetType.Directory => DotnetTestSetup.TestProjectSourcesDirPath,
            CommandTargetType.Project => $"{DotnetTestSetup.TestProjectSourcesDirPath}/{projectName}.csproj",
            CommandTargetType.Solution => $"{DotnetTestSetup.TestProjectSourcesDirPath}/{projectName}.sln",
            CommandTargetType.Assembly => $"{DotnetTestSetup.TestProjectSourcesDirPath}/bin/Debug/{targetFrameworkMoniker}/{projectName}.dll",
            _ => throw new ArgumentOutOfRangeException(nameof(targetType), targetType, null)
        };

        return new ParallelTestRunTarget(
            QueriesFilePath: DotnetTestSetup.TestsQueriesFilePath,
            TargetPath: targetPath,
            MsBuildBinLogPath: withMsBuildBinaryLog ? binlogPath : null
        );
    }

    public async Task<(ExecResult, IReadOnlyList<string>)> RunTests(string targetPath)
    {
        var trxReportFilePath = $"{DotnetTestSetup.TestProjectSourcesDirPath}/report__{Guid.NewGuid()}.trx";
        var testOutput = await ExecAsync($"dotnet test {targetPath} --no-build -l:trx;LogFileName={trxReportFilePath}");
        var testNamesExecuted = await GetTestNames(trxReportFilePath);
        return (testOutput, testNamesExecuted);
    }

    public Task<ExecResult> RunFilterApp(string argsStr) =>
        ExecAsync($"dotnet --roll-forward LatestMajor {DotnetTestSetup.AppPath} {argsStr}");


    private async Task<IReadOnlyList<string>> GetTestNames(string trxReportFilePath)
    {
        // parse trx report
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

    private async Task Prepare()
    {
        Output.WriteLine($"Preparing container {Container.Name}...");
        
        // filter app
        var appDirCreateResult = await ExecAsync($"mkdir {DotnetTestSetup.AppDirPath}");
        if (appDirCreateResult.ExitCode == 0)
        {
            await ExecAsync($"cp -a {DotnetTestSetup.MountedFilterAppSourcesDirPath}/net6.0/. {DotnetTestSetup.AppDirPath}");
        }

        // test project
        var testDirCreateResult = await ExecAsync($"mkdir {DotnetTestSetup.TestProjectSourcesDirPath}");
        if (testDirCreateResult.ExitCode != 0)
        {
            // clean up previous project
            TestSetup.ClearTestProjectDir();
            await ExecAsync($"rm -r {DotnetTestSetup.TestProjectSourcesDirPath}");
        }
        
        Output.WriteLine($"Container {Container.Name} prepared for test");
    }

    public void Dispose()
    {
        foreach (var testSetup in _testSetup)
        {
            testSetup.Value.Dispose();
        }
        _testSetup.Clear();
    }
}

public record class ParallelTestRunTarget(
    string QueriesFilePath,
    string TargetPath,
    string? MsBuildBinLogPath
);