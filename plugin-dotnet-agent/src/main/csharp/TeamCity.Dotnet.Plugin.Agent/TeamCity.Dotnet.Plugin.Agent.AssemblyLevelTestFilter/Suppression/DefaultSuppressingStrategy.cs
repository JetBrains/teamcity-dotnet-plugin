using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;

internal abstract class DefaultSuppressingStrategy<TTestEngine> : ITestSuppressingStrategy
    where TTestEngine : ITestEngine
{
    protected DefaultSuppressingStrategy(TTestEngine testEngine)
    {
        TestEngine = testEngine;
    }
    
    protected TTestEngine TestEngine { get; }
    
    protected IEnumerable<MethodDefinition> GetTestMethods(TypeDefinition type) =>
        type.Methods
            .Where(method => method.CustomAttributes
                .Select(a => a.AttributeType.FullName)
                .Any(TestEngine.TestMethodAttributes.Contains)
            );
    
    protected void RemoveAllTestAttributes(TypeDefinition type)
    {
        RemoveTestAttributesFromMethods(type);
        RemoveTestAttributesFromClass(type);
    }

    protected void RemoveTestAttributesFromMethods(TypeDefinition testClass)
    {
        foreach (var method in GetTestMethods(testClass))
        {
            foreach (var testAttribute in GetMethodsTestAttributes(method))
            {
                method.CustomAttributes.Remove(testAttribute);
            }
        }
    }

    protected void RemoveTestAttributesFromClass(TypeDefinition testClass)
    {
        foreach (var testAttribute in GetTypeTestAttributes(testClass))
        {
            testClass.CustomAttributes.Remove(testAttribute);
        }
    }

    private List<CustomAttribute> GetMethodsTestAttributes(MethodDefinition method)
    {
        return method.CustomAttributes
            .Where(a => TestEngine.TestMethodAttributes.Contains(a.AttributeType.FullName))
            .ToList();
    }
    
    private List<CustomAttribute> GetTypeTestAttributes(TypeDefinition testClass)
    {
        return testClass.CustomAttributes
            .Where(a => TestEngine.TestClassAttributes.Contains(a.AttributeType.FullName))
            .ToList();
    }
}