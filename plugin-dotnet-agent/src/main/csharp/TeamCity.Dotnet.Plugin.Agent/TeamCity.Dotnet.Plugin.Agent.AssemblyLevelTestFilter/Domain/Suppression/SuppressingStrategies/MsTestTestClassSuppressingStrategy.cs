using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.Engines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression.SuppressingStrategies;

internal class MsTestTestClassSuppressingStrategy : BaseSuppressingStrategy<MsTest, TestClassSelector>
{
    public MsTestTestClassSuppressingStrategy(MsTest testEngine) : base(testEngine) {}
}