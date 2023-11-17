using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressionStrategies;

internal class XUnitTestClassSuppressionStrategy : BaseSuppressionStrategy<XUnit>
{
    public XUnitTestClassSuppressionStrategy(XUnit testEngine) : base(testEngine) {}
}