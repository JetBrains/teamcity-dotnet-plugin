using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal interface ITestSuppressingStrategy
{
    TestSuppressionResult SuppressTests(IDotnetType type, ITestSelector testSelector);
}

internal interface ITestSuppressingStrategy<TTestEngine, in TTestSelector> : ITestSuppressingStrategy
    where TTestEngine : ITestEngine
    where TTestSelector : ITestSelector
{
    TestSuppressionResult SuppressTestsBySelector(IDotnetType type, TTestSelector testSelector);
}