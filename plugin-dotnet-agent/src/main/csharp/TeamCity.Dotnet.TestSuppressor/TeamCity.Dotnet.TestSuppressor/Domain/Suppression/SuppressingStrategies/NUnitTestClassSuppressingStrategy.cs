using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressingStrategies;

internal class NUnitTestClassSuppressingStrategy : BaseSuppressingStrategy<NUnit, TestClassSelector>
{
    public NUnitTestClassSuppressingStrategy(NUnit testEngine) : base(testEngine) {}
}