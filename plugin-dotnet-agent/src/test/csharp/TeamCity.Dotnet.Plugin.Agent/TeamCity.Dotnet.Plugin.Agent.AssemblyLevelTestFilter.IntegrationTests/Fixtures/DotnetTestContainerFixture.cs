/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System.Collections.Concurrent;
using System.Diagnostics;
using System.Xml.Linq;
using DotNet.Testcontainers.Containers;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures;

public class DotnetTestContainerFixture : IDisposable
{
    private readonly ConcurrentDictionary<DotnetVersion, DotnetTestSetup> _testSetup;
    private DotnetVersion _dotnetVersion = DotnetVersion.v8_0_Preview;
    
    private IContainer Container => _testSetup[_dotnetVersion].Container;
    
    private DotnetTestSetup TestSetup  => _testSetup[_dotnetVersion];

    public ITestOutputHelper Output { get; set; } = null!;
    
    public DotnetTestContainerFixture()
    {
        _testSetup = new ConcurrentDictionary<DotnetVersion, DotnetTestSetup>();
        Parallel.ForEach(Enum.GetValues<DotnetVersion>(), dotnetVersion =>
        {
            var testSetup = new DotnetTestSetup(dotnetVersion);
            _testSetup.TryAdd(dotnetVersion, testSetup);
        });
    }

    public async Task ReinitContainerWith(DotnetVersion dotnetVersion)
    {
        _dotnetVersion = dotnetVersion;
        await Prepare();
    }
    
    public async Task<ExecResult> ExecAsync(string command, bool silent = false)
    {
        string Format(string value, string linePrefix = "  ")
        {
            var lines = value.Trim().Split('\n').Select(x => linePrefix + x);
            return string.Join('\n', lines);
        }

        Output.WriteLine("> " + command);
        var watch = Stopwatch.StartNew();
        var result = await Container.ExecAsync(command.Split(" "));
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

    public async Task<(string queriesFilePath, string targetDllPath)> CreateTestProject(
        Type testProjectType,
        DotnetVersion dotnetVersion,
        string projectName,
        TestClassDescription[] projectTestClasses,
        params TestClassDescription[] testQueriesFileTestClasses
    ) {
        // generate test project
        var testProject = (ITestProject) Activator.CreateInstance(testProjectType)!;
        await testProject.GenerateAsync(_dotnetVersion, TestSetup.HostTestProjectPath, projectName, projectTestClasses);
        
        // generate tests queries file
        var content = string.Join('\n', testQueriesFileTestClasses.Select(tc => $"{projectName}.{tc.ClassName}"));
        await File.WriteAllTextAsync(TestSetup.HostTestsQueriesFilePath, content);
        
        // copy test project and tests queries file to container
        await ExecAsync($"cp -a {DotnetTestSetup.MountedTestProjectSourcesDirPath}/. {DotnetTestSetup.TestProjectSourcesDirPath}");
        var testProjectBuildResult = await ExecAsync($"dotnet build {DotnetTestSetup.TestProjectSourcesDirPath}");
        if (testProjectBuildResult.ExitCode != 0)
        {
            throw new Exception("Failed to build test project:\n" + testProjectBuildResult.Stderr);
        }
        var moniker = dotnetVersion.GetMoniker();
        var targetDllPath = $"{DotnetTestSetup.TestProjectSourcesDirPath}/bin/Debug/{moniker}/{projectName}.dll";
        
        return (DotnetTestSetup.TestsQueriesFilePath, targetDllPath);
    }

    public async Task<(ExecResult, IReadOnlyList<string>)> RunTests(string targetAssemblyPath)
    {
        var trxReportFilePath = $"{DotnetTestSetup.TestProjectSourcesDirPath}/report__{Guid.NewGuid()}.trx";
        var testOutput = await ExecAsync($"dotnet test {targetAssemblyPath} -l:trx;LogFileName={trxReportFilePath}");
        var testNamesExecuted = await GetTestNames(trxReportFilePath);
        return (testOutput, testNamesExecuted);
    }

    public Task RunFilterApp(string argsStr) => ExecAsync($"dotnet {DotnetTestSetup.AppPath} {argsStr}");

    public async Task<IReadOnlyList<string>> GetTestNames(string trxReportFilePath)
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
        Output.WriteLine($"Reinitializing container {Container.Name}...");
        
        // filter app
        var appDirCreateResult = await ExecAsync($"mkdir {DotnetTestSetup.AppDirPath}");
        if (appDirCreateResult.ExitCode == 0)
        {
            await ExecAsync($"cp -a {DotnetTestSetup.MountedFilterAppSourcesDirPath}/{_dotnetVersion.GetMoniker()}/. {DotnetTestSetup.AppDirPath}");
        }

        // test project
        var testDirCreateResult = await ExecAsync($"mkdir {DotnetTestSetup.TestProjectSourcesDirPath}");
        if (testDirCreateResult.ExitCode != 0)
        {
            // clean up previous project
            TestSetup.ClearTestProjectDir();
            await ExecAsync($"rm -r {DotnetTestSetup.TestProjectSourcesDirPath}");
        }
        
        Output.WriteLine($"Container {Container.Name} reinitialized");
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