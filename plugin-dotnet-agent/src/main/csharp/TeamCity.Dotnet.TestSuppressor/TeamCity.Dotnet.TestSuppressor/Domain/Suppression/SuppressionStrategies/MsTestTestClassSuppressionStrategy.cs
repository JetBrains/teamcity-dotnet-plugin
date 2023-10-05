using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressionStrategies;

internal class MsTestTestClassSuppressionStrategy : BaseSuppressionStrategy<MsTest, TestClassSelector>
{
    public MsTestTestClassSuppressionStrategy(MsTest testEngine) : base(testEngine) {}
}