using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression;

internal interface ITestSuppressingStrategy
{
    Type TestEngineType { get; }
    
    Type TestSelectorType { get; }
    
    TestSuppressionResult SuppressTests(IDotnetType type, ITestSelector testSelector);
}