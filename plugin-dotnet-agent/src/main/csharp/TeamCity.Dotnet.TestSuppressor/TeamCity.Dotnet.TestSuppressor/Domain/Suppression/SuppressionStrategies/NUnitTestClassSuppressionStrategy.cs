using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressionStrategies;

internal class NUnitTestClassSuppressionStrategy : BaseSuppressionStrategy<NUnit>
{
    public NUnitTestClassSuppressionStrategy(NUnit testEngine) : base(testEngine) {}
}