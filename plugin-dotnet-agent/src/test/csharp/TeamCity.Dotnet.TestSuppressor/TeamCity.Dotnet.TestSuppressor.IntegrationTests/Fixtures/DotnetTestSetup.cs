using System.Collections.Concurrent;
using DotNet.Testcontainers.Builders;
using DotNet.Testcontainers.Containers;
using DotNet.Testcontainers.Images;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;

internal class DotnetTestSetup : IDisposable
{
    private static readonly Lazy<ConcurrentDictionary<DotnetVersion, IImage>> LazyDockerImages;
    
    private const string WorkDirPath = "/app";
    private const string TestsQueriesFileName = "tests-list.txt";
    private readonly string _hostAppSourcesPath = $"{CurrentDirectory}/published-app-binaries";
    private const string AppName = "TeamCity.Dotnet.TestSuppressor.dll";

    private readonly Lazy<IContainer> _container;

    static DotnetTestSetup()
    {
        LazyDockerImages = new Lazy<ConcurrentDictionary<DotnetVersion, IImage>>(() =>
        {
            var result = new ConcurrentDictionary<DotnetVersion, IImage>();
            
            // can't made it in parallel because of Docker Wormhole configuration fails to run in parallel
            // it looks like deadlock in Testcontainers or Docker SDK
            foreach(var dv in Enum.GetValues<DotnetVersion>())
            {
                result.TryAdd(dv, new DotnetSdkImage(dv));
            }
            
            return result;
        }, LazyThreadSafetyMode.ExecutionAndPublication);
    }

    private static ConcurrentDictionary<DotnetVersion, IImage> DockerImages => LazyDockerImages.Value;
    
    private static string CurrentDirectory => Directory.GetCurrentDirectory();

    public DotnetTestSetup(DotnetVersion dotnetVersion)
    {
        var id = Guid.NewGuid();
        HostTestProjectPath = Directory.CreateDirectory($"{CurrentDirectory}/test-project__dotnet_{dotnetVersion}__{id}").FullName;
        _container = new Lazy<IContainer>(
            () => RunContainer(dotnetVersion, id).ConfigureAwait(false).GetAwaiter().GetResult(),
            LazyThreadSafetyMode.ExecutionAndPublication
        );
    }

    public static string MountedTestProjectSourcesDirPath => "/mounted__test-project-sources";

    public static string MountedFilterAppSourcesDirPath => "/mounted__app-binaries";

    public static string TestProjectSourcesDirPath => WorkDirPath + "/test-project";

    public static string AppDirPath => WorkDirPath + "/filter-app";

    public static string AppPath => AppDirPath + "/" + AppName;

    public string HostTestProjectPath { get; }

    public string HostTestsQueriesFilePath => $"{HostTestProjectPath}/{TestsQueriesFileName}";

    public static string TestsQueriesFilePath => $"{TestProjectSourcesDirPath}/{TestsQueriesFileName}";

    public IContainer Container => _container.Value;

    public void ClearTestProjectDir() => ClearDirectory(HostTestProjectPath);

    private async Task<IContainer> RunContainer(DotnetVersion dotnetVersion, Guid id)
    {
        var container = new ContainerBuilder()
            .WithImage(DockerImages[dotnetVersion])
            .WithName($"tc-dotnet-test-suppressor__integration-tests__dotnet_{dotnetVersion}__{id}")
            .WithWorkingDirectory(WorkDirPath)
            .WithCommand("tail", "-f", "/dev/null")
            .WithBindMount(HostTestProjectPath, MountedTestProjectSourcesDirPath)
            .WithBindMount(_hostAppSourcesPath, MountedFilterAppSourcesDirPath)
            .WithCleanUp(true)
            .WithAutoRemove(true)
            .Build();
        await container.StartAsync().ConfigureAwait(false);
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
        Container.DisposeAsync().AsTask().ConfigureAwait(false).GetAwaiter().GetResult();

        ClearDirectory(HostTestProjectPath);
        Directory.Delete(HostTestProjectPath);
    }
}