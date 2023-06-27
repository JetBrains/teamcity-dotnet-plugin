using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;

internal class TestEngineRecognizer : ITestEngineRecognizer
{
    private readonly IEnumerable<ITestEngine> _testEngines;

    public TestEngineRecognizer(IEnumerable<ITestEngine> testEngines)
    {
        _testEngines = testEngines;
    }

    public IList<ITestEngine> RecognizeTestEngines(IDotnetType type) =>
        _testEngines.Where(engine => HasTestClassAttribute(engine, type) || HasTestMethodAttribute(engine, type)).ToList();

    private static bool HasTestClassAttribute(ITestEngine engine, IDotnetType type) =>
        type.CustomAttributes.Any(attr => engine.TestClassAttributes.Contains(attr.FullName));

    private static bool HasTestMethodAttribute(ITestEngine engine, IDotnetType type) =>
        type.Methods.Any(method => method.CustomAttributes.Any(attr => engine.TestMethodAttributes.Contains(attr.FullName)));
}