using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressingStrategies;

internal class MsTestTestClassSuppressingStrategy : BaseSuppressingStrategy<MsTest, TestClassSelector>
{
    public MsTestTestClassSuppressingStrategy(MsTest testEngine) : base(testEngine) {}
}