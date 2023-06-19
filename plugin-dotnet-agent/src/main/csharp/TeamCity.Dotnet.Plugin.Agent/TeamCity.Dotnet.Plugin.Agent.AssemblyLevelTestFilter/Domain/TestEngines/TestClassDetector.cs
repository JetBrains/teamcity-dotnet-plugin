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
        .Select(type => new TestClass(type, _testEngineRecognizer.RecognizeTestEngines(type)))
        .Where(testClass => testClass.TestEngines.Any());
}