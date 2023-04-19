using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Options;
using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter;

internal class AssemblyPatcher : IHostedService
{
    private readonly Settings _settings;
    private readonly ITestsSuppressionResolver _testsSuppressionResolver;
    private readonly ITestEngineRecognizer _testEngineRecognizer;
    private readonly ITestsSuppressor _testsSuppressor;
    
    public AssemblyPatcher(
        IOptions<Settings> settings,
        ITestsSuppressionResolver testsSuppressionResolver,
        ITestEngineRecognizer testEngineRecognizer,
        ITestsSuppressor testsSuppressor)
    {
        _testsSuppressionResolver = testsSuppressionResolver;
        _testEngineRecognizer = testEngineRecognizer;
        _testsSuppressor = testsSuppressor;
        _settings = settings.Value;
    }

    public Task StartAsync(CancellationToken cancellationToken)
    {
        var patchTasks = _settings.InputAssembliesPaths
            .Select(assemblyPath => Task.Run(() => PatchAssembly(assemblyPath), cancellationToken));
        return Task.WhenAll(patchTasks);
    }

    public Task StopAsync(CancellationToken cancellationToken) => Task.CompletedTask;

    private void PatchAssembly(string assemblyPath)
    {
        var assembly = LoadAssembly(assemblyPath);

        var testClasses = assembly.Modules.SelectMany(module => module.Types)
            .GroupBy(type => _testEngineRecognizer.RecognizeTestEngines(type))
            .Where(typesByTestEngine => typesByTestEngine.Key.Any()) // filter out types that are not test classes
            .SelectMany(testClassesByTestEngine =>
            {
                var testEngine = testClassesByTestEngine.Key!;
                return testClassesByTestEngine.Select(testClass => (testClass, testEngine));
            });
        
        foreach (var (testClass, detectedTestEngines) in testClasses)
        {
            var suppressionCriterion = _testsSuppressionResolver.ResolveCriteria(testClass.FullName);

            foreach (var testEngine in detectedTestEngines)
            {
                _testsSuppressor.SuppressTests(testClass, testEngine, suppressionCriterion);
            }
        }
        
        SaveAssembly(assembly, _settings.OutputAssembliesPath);
    }

    private static AssemblyDefinition LoadAssembly(string assemblyPath)
    {
        var assemblyResolver = new DefaultAssemblyResolver();
        assemblyResolver.AddSearchDirectory(AppDomain.CurrentDomain.BaseDirectory);
        return AssemblyDefinition.ReadAssembly(assemblyPath, new ReaderParameters
        {
            AssemblyResolver = assemblyResolver,
            ReadSymbols = true,                     // read debug symbols if available
            ReadWrite = true,                       // allow writing to the assembly
        });
    }

    private static void SaveAssembly(AssemblyDefinition assembly, string outputAssemblyPath)
    {
        outputAssemblyPath += "_tmp";

        // save the modified assembly on disk
        assembly.Write(outputAssemblyPath, new WriterParameters
        {
            WriteSymbols = true, // preserve debug symbols if available
        });

        File.Move(outputAssemblyPath, outputAssemblyPath[..^4], overwrite: true);
    }
}
