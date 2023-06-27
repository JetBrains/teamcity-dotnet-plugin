using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression;

internal record struct TestSuppressionParameters(ITestEngine TestEngine, ITestSelector TestSelector);
