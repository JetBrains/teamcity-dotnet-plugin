using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.NUnit;

internal class NUnitTestClassSuppressingStrategy : BaseSuppressingStrategy<NUnit>, ITestSuppressingStrategy<NUnit, TestClassSelector>
{
    public NUnitTestClassSuppressingStrategy(NUnit testEngine) : base(testEngine) {}

    public void SuppressTests(TypeDefinition type, TestClassSelector testsSelector) => RemoveAllTestAttributes(type);
}