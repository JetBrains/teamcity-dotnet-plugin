using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.MsTest;

internal class MsTestTestClassSuppressingStrategy : BaseSuppressingStrategy<MsTest>, ITestSuppressingStrategy<MsTest, TestClassSelector>
{
    public MsTestTestClassSuppressingStrategy(MsTest testEngine) : base(testEngine) {}

    public void SuppressTests(TypeDefinition type, TestClassSelector testsSelector) => RemoveAllTestAttributes(type);
}