using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression;

internal interface ITestSuppressionStrategy
{
    Type TestEngineType { get; }

    TestSuppressionResult SuppressTests(IDotnetType type);
}