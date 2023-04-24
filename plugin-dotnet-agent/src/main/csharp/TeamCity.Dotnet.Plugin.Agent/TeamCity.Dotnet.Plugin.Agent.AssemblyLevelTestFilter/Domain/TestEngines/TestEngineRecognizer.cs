using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

internal class TestEngineRecognizer : ITestEngineRecognizer
{
    private readonly IEnumerable<ITestEngine> _testEngines;

    public TestEngineRecognizer(IEnumerable<ITestEngine> testEngines)
    {
        _testEngines = testEngines;
    }

    public IList<ITestEngine> RecognizeTestEngines(TypeDefinition type) =>
        _testEngines.Where(engine => HasTestClassAttribute(engine, type) || HasTestMethodAttribute(engine, type)).ToList();

    private static bool HasTestClassAttribute(ITestEngine engine, TypeDefinition type) =>
        type.CustomAttributes.Any(attr => engine.TestClassAttributes.Contains(attr.AttributeType.FullName));

    private static bool HasTestMethodAttribute(ITestEngine engine, TypeDefinition type) =>
        type.Methods.Any(method => method.CustomAttributes.Any(attr => engine.TestMethodAttributes.Contains(attr.AttributeType.FullName)));
}