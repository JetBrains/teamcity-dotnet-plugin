using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressionStrategies;

internal class MsTestTestClassSuppressionStrategy : BaseSuppressionStrategy<MsTest>
{
    public MsTestTestClassSuppressionStrategy(MsTest testEngine) : base(testEngine) {}
}