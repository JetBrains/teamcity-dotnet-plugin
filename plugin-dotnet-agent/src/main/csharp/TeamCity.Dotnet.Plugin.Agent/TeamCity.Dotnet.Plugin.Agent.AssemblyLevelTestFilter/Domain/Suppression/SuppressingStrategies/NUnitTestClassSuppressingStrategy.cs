using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.Engines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression.SuppressingStrategies;

internal class NUnitTestClassSuppressingStrategy : BaseSuppressingStrategy<NUnit, TestClassSelector>, ITestSuppressingStrategy<NUnit, TestClassSelector>
{
    public NUnitTestClassSuppressingStrategy(NUnit testEngine) : base(testEngine) {}

    
    public override TestSuppressionResult SuppressTestsBySelector(IDotnetType type, TestClassSelector testSelector) =>
        RemoveAllTestAttributes(type);
}