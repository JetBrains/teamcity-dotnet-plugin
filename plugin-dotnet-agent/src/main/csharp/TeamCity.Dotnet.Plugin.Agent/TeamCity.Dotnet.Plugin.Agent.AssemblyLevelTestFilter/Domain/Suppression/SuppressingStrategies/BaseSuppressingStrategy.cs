using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression.SuppressingStrategies;

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
        var (suppressedTests, suppressedClasses) = RemoveTestAttributesFromClass(type);
        suppressedTests += RemoveTestAttributesFromMethods(type);
        return new TestSuppressionResult(suppressedTests, suppressedClasses);
    }

    public abstract TestSuppressionResult SuppressTestsBySelector(IDotnetType type, TTestSelector testSelector);

    public TestSuppressionResult SuppressTests(IDotnetType type, ITestSelector testSelector) =>
        SuppressTestsBySelector(type, (TTestSelector) testSelector);

    private int RemoveTestAttributesFromMethods(IDotnetType testClass)
    {
        var suppressedTests = 0;
        foreach (var method in GetTestMethods(testClass))
        {
            foreach (var testAttribute in GetMethodsTestAttributes(method))
            {
                method.RemoveCustomAttribute(testAttribute);
            }
            suppressedTests++;
        }

        return suppressedTests;
    }

    private (int, int) RemoveTestAttributesFromClass(IDotnetType testClass)
    {
        var (suppressedTests, suppressedClasses) = (0, 0);
        foreach (var testAttribute in GetTypeTestAttributes(testClass))
        {
            testClass.RemoveCustomAttribute(testAttribute);
            suppressedClasses++;
        }
        return (suppressedTests, suppressedClasses);
    }

    private List<IDotnetCustomAttribute> GetMethodsTestAttributes(IDotnetMethod method)
    {
        return method.CustomAttributes
            .Where(a => TestEngine.TestMethodAttributes.Contains(a.FullName))
            .ToList();
    }

    private List<IDotnetCustomAttribute> GetTypeTestAttributes(IDotnetType testClass)
    {
        return testClass.CustomAttributes
            .Where(a => TestEngine.TestClassAttributes.Contains(a.FullName))
            .ToList();
    }
}