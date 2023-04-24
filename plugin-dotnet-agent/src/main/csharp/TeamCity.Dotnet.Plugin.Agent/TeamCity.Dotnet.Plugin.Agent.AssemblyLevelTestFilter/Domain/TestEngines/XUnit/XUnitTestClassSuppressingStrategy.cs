using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.XUnit;

internal class XUnitTestClassSuppressingStrategy : BaseSuppressingStrategy<XUnit>, ITestSuppressingStrategy<XUnit, TestClassSelector>
{
    public XUnitTestClassSuppressingStrategy(XUnit testEngine) : base(testEngine) {}

    public void SuppressTests(TypeDefinition type, TestClassSelector testsSelector) => RemoveAllTestAttributes(type);
}