using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressionStrategies;

internal class NUnitTestClassSuppressionStrategy : BaseSuppressionStrategy<NUnit, TestClassSelector>
{
    public NUnitTestClassSuppressionStrategy(NUnit testEngine) : base(testEngine) {}
}