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

using System.Diagnostics;
using System.Xml.Linq;
using DotNet.Testcontainers.Builders;
using DotNet.Testcontainers.Configurations;
using DotNet.Testcontainers.Containers;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.TestsGenerators;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class DotnetContainerFixture : IDisposable
{
    private readonly string _hostTestProjectPath = $"{Directory.GetCurrentDirectory()}_{Guid.NewGuid()}/test-project";
    private readonly string _hostAppSourcesPath = $"{Directory.GetCurrentDirectory()}/sources";
    private const string MountedTestProjectSourcesDirPath = "/test-project-sources";
    private const string MountedFilterAppSourcesDirPath = "/filter-app-sources";
    private const string WorkDirPath = "/app";
    private const string TestProjectSourcesDirPath = WorkDirPath + "/test-project";
    private const string AppDirPath = WorkDirPath + "/filter-app";
    private const string AppSourcesDirPath = AppDirPath + "/sources";
    private const string AppName = "TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter";
    private const string AppPath = AppDirPath + "/" + AppName;
    private const string TestsQueriesFileName = "tests-list.txt";
    private readonly IContainer _container;
    private ITestOutputHelper? _output;

    private ITestOutputHelper Output => _output!;
    
    public DotnetContainerFixture()
    {
        Directory.CreateDirectory(_hostTestProjectPath);
        _container = new ContainerBuilder()
            .WithImage("mcr.microsoft.com/dotnet/sdk:8.0-preview")
            .WithName($"tc-dotnet-plugin-agent-tests-filter__integration-tests__{Guid.NewGuid().ToString()[..8]}")
            .WithWorkingDirectory(WorkDirPath)
            .WithCommand("tail", "-f", "/dev/null")
            .WithBindMount(_hostTestProjectPath, MountedTestProjectSourcesDirPath, AccessMode.ReadOnly)
            .WithBindMount(_hostAppSourcesPath, MountedFilterAppSourcesDirPath, AccessMode.ReadOnly)
            .Build();
        _container.StartAsync().Wait();
    }

    public void Init(ITestOutputHelper output)
    {
        _output = output;
        Prepare().Wait();
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
        var result = await _container.ExecAsync(command.Split(" "));
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
        Type testProjectGeneratorType, string projectName, TestClassDescription[] projectTestClasses, params TestClassDescription[] testQueriesFileTestClasses)
    {
        // generate test project
        var generator = GetTestProjectGenerator(testProjectGeneratorType);
        await generator.GenerateAsync(_hostTestProjectPath, projectName, projectTestClasses);
        
        // generate tests queries file
        var content = string.Join('\n', testQueriesFileTestClasses.Select(tc => $"{projectName}.{tc.ClassName}"));
        await File.WriteAllTextAsync($"{_hostTestProjectPath}/{TestsQueriesFileName}", content);
        var queriesFilePath = $"{TestProjectSourcesDirPath}/{TestsQueriesFileName}";
        
        // copy test project and tests queries file to container
        await ExecAsync($"cp -a {MountedTestProjectSourcesDirPath}/. {TestProjectSourcesDirPath}");
        await ExecAsync($"dotnet build {TestProjectSourcesDirPath}");
        var targetDllPath = $"{TestProjectSourcesDirPath}/bin/Debug/net8.0/{projectName}.dll";
        
        return (queriesFilePath, targetDllPath);
    }

    public async Task<(ExecResult, IReadOnlyList<string>)> RunTests(string targetAssemblyPath)
    {
        var trxReportFilePath = $"{TestProjectSourcesDirPath}/report__{Guid.NewGuid()}.trx";
        var testOutput = await ExecAsync($"dotnet test {targetAssemblyPath} -l:trx;LogFileName={trxReportFilePath}");
        var testNamesExecuted = await GetTestNames(trxReportFilePath);
        return (testOutput, testNamesExecuted);
    }

    public Task RunFilterApp(string argsStr) => ExecAsync($"{AppPath} {argsStr}");

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
        // filter app
        await ExecAsync($"mkdir {AppDirPath}");
        await ExecAsync($"mkdir {AppSourcesDirPath}");
        await ExecAsync($"cp -a {MountedFilterAppSourcesDirPath}/. {AppSourcesDirPath}");
        await ExecAsync($"dotnet publish {AppSourcesDirPath} -c Release -o {AppDirPath} --verbosity quiet");
        
        // test project
        await ExecAsync($"mkdir {TestProjectSourcesDirPath}");
        await ExecAsync($"cp -a {MountedTestProjectSourcesDirPath}/. {TestProjectSourcesDirPath}");
    }
    
    private static ITestProjectGenerator GetTestProjectGenerator(Type testProjectGeneratorType) =>
        (ITestProjectGenerator) Activator.CreateInstance(testProjectGeneratorType)!;

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

    public void Dispose()
    {
        _container.DisposeAsync().AsTask().Wait();

        DeleteDirectoryContents(_hostTestProjectPath);
        Directory.Delete(_hostTestProjectPath);
    }
}