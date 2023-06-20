using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.Engines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression.SuppressingStrategies;

internal class NUnitTestClassSuppressingStrategy : BaseSuppressingStrategy<NUnit, TestClassSelector>
{
    public NUnitTestClassSuppressingStrategy(NUnit testEngine) : base(testEngine) {}
}