using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

internal class TestClassDetector : ITestClassDetector
{
    private readonly ITestEngineRecognizer _testEngineRecognizer;

    public TestClassDetector(ITestEngineRecognizer testEngineRecognizer)
    {
        _testEngineRecognizer = testEngineRecognizer;
    }
    
    public IEnumerable<TestClass> Detect(IDotnetAssembly assembly) => assembly.Types
        .GroupBy(type => _testEngineRecognizer.RecognizeTestEngines(type))
        .Where(typesByTestEngine => typesByTestEngine.Key.Any()) // filter out types that are not test classes
        .SelectMany(testClassesByTestEngine =>
        {
            var testEngine = testClassesByTestEngine.Key!;
            return testClassesByTestEngine.Select(testClassType => new TestClass(testClassType, testEngine));
        });
}