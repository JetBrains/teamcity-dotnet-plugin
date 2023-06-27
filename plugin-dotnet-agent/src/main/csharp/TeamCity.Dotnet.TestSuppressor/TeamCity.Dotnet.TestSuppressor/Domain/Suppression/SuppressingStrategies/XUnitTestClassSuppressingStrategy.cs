using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressingStrategies;

internal class XUnitTestClassSuppressingStrategy : BaseSuppressingStrategy<XUnit, TestClassSelector>
{
    public XUnitTestClassSuppressingStrategy(XUnit testEngine) : base(testEngine) {}
}