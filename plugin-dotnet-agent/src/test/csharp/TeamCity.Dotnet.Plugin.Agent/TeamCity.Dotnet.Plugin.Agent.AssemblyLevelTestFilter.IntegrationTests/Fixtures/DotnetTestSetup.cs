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
using DotNet.Testcontainers.Builders;
using DotNet.Testcontainers.Configurations;
using DotNet.Testcontainers.Containers;
using DotNet.Testcontainers.Images;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures;

internal class DotnetTestSetup : IDisposable
{
    private static object _lock = new();
    private static readonly ConcurrentDictionary<DotnetVersion, IImage> DockerImages;
    private const string WorkDirPath = "/app";
    private const string TestsQueriesFileName = "tests-list.txt";
    private readonly string _hostAppSourcesPath = $"{CurrentDirectory}/published-app-binaries";
    private const string AppName = "TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.dll";

    static DotnetTestSetup()
    {
        DockerImages = new ConcurrentDictionary<DotnetVersion, IImage>();
    }

    private static string CurrentDirectory => Directory.GetCurrentDirectory();

    public DotnetTestSetup(DotnetVersion dotnetVersion)
    {
        if (!DockerImages.ContainsKey(dotnetVersion))
        {
            lock (_lock)
            {
                if (!DockerImages.ContainsKey(dotnetVersion))
                {
                    Parallel.ForEach(Enum.GetValues<DotnetVersion>(), dv =>
                    {
                        DockerImages.TryAdd(dv, new DotnetSdkImage(dv));
                    });
                }
            }
        }

        var id = Guid.NewGuid();
        HostTestProjectPath = Directory.CreateDirectory($"{CurrentDirectory}/test-project__dotnet_{dotnetVersion}__{id}").FullName;
        Container = RunContainer(dotnetVersion, id).Result;
    }

    public static string MountedTestProjectSourcesDirPath => "/mounted__test-project-sources";

    public static string MountedFilterAppSourcesDirPath => "/mounted__app-binaries";

    public static string TestProjectSourcesDirPath => WorkDirPath + "/test-project";

    public static string AppDirPath => WorkDirPath + "/filter-app";

    public static string AppPath => AppDirPath + "/" + AppName;

    public string HostTestProjectPath { get; }

    public string HostTestsQueriesFilePath => $"{HostTestProjectPath}/{TestsQueriesFileName}";

    public static string TestsQueriesFilePath => $"{TestProjectSourcesDirPath}/{TestsQueriesFileName}";

    public IContainer Container { get; }

    public void ClearTestProjectDir() => ClearDirectory(HostTestProjectPath);

    private async Task<IContainer> RunContainer(DotnetVersion dotnetVersion, Guid id)
    {
        var container = new ContainerBuilder()
            .WithImage(DockerImages[dotnetVersion])
            .WithName($"tc-dotnet-plugin-agent-tests-filter__integration-tests__dotnet_{dotnetVersion}__{id}")
            .WithWorkingDirectory(WorkDirPath)
            .WithCommand("tail", "-f", "/dev/null")
            .WithBindMount(HostTestProjectPath, MountedTestProjectSourcesDirPath, AccessMode.ReadOnly)
            .WithBindMount(_hostAppSourcesPath, MountedFilterAppSourcesDirPath, AccessMode.ReadOnly)
            .WithCleanUp(true)
            .WithAutoRemove(true)
            .Build();
        await container.StartAsync();
        return container;
    }

    private static void ClearDirectory(string directoryPath)
    {
        foreach (var file in Directory.GetFiles(directoryPath))
        {
            File.Delete(file);
        }

        foreach (var subdirectory in Directory.GetDirectories(directoryPath))
        {
            ClearDirectory(subdirectory);
        }
    }

    public void Dispose()
    {
        Container.DisposeAsync().AsTask().Wait();

        ClearDirectory(HostTestProjectPath);
        Directory.Delete(HostTestProjectPath);
    }
}