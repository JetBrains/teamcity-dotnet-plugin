using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal interface ITestSuppressingStrategy
{
    Type TestEngineType { get; }
    
    Type TestSelectorType { get; }
    
    TestSuppressionResult SuppressTests(IDotnetType type, ITestSelector testSelector);
}