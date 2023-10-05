using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressionStrategies;

internal class XUnitTestClassSuppressionStrategy : BaseSuppressionStrategy<XUnit, TestClassSelector>
{
    public XUnitTestClassSuppressionStrategy(XUnit testEngine) : base(testEngine) {}
}