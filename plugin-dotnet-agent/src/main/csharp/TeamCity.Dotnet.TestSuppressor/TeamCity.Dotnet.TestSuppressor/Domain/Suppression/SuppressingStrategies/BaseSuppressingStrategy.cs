using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressingStrategies;

internal abstract class BaseSuppressingStrategy<TTestEngine, TTestSelector> : ITestSuppressingStrategy
    where TTestEngine : ITestEngine
    where TTestSelector : ITestSelector
{
    protected BaseSuppressingStrategy(TTestEngine testEngine)
    {
        TestEngine = testEngine;
    }

    private TTestEngine TestEngine { get; }
    
    private IEnumerable<IDotnetMethod> GetTestMethods(IDotnetType type) =>
        type.Methods
            .Where(method => method.CustomAttributes
                .Select(a => a.FullName)
                .Any(TestEngine.TestMethodAttributes.Contains)
            );

    protected TestSuppressionResult RemoveAllTestAttributes(IDotnetType type)
    {
        var suppressedClasses = RemoveTestAttributesFromClass(type);
        var suppressedTests = RemoveTestAttributesFromMethods(type);
        return new TestSuppressionResult(suppressedTests, suppressedClasses);
    }

    public Type TestEngineType => typeof(TTestEngine);
    
    public Type TestSelectorType => typeof(TTestSelector);

    public TestSuppressionResult SuppressTests(IDotnetType type, ITestSelector testSelector) =>
        SuppressTestsBySelector(type, (TTestSelector) testSelector);

    private TestSuppressionResult SuppressTestsBySelector(IDotnetType type, TTestSelector testSelector) =>
        RemoveAllTestAttributes(type);

    private int RemoveTestAttributesFromMethods(IDotnetType testClass)
    {
        var suppressedTests = 0;
        foreach (var method in GetTestMethods(testClass))
        {
            foreach (var testAttribute in GetTestMethodAttributes(method))
            {
                method.RemoveCustomAttribute(testAttribute);
            }
            suppressedTests++;
        }

        return suppressedTests;
    }

    private int RemoveTestAttributesFromClass(IDotnetType testClass)
    {
        var suppressedClasses = 0;
        foreach (var testAttribute in GetTestClassAttributes(testClass))
        {
            testClass.RemoveCustomAttribute(testAttribute);
            suppressedClasses++;
        }
        return suppressedClasses;
    }

    private List<IDotnetCustomAttribute> GetTestMethodAttributes(IDotnetMethod method) => method.CustomAttributes
        .Where(a => TestEngine.TestMethodAttributes.Contains(a.FullName))
        .ToList();

    private List<IDotnetCustomAttribute> GetTestClassAttributes(IDotnetType testClass) => testClass.CustomAttributes
        .Where(a => TestEngine.TestClassAttributes.Contains(a.FullName))
        .ToList();
}