using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal interface ITestSuppressingStrategy {}

internal interface ITestSuppressingStrategy<TTestEngine, in TTestsQuery> : ITestSuppressingStrategy
    where TTestEngine : ITestEngine
    where TTestsQuery : ITestsSelector
{
    public void SuppressTests(TypeDefinition type, TTestsQuery testsQuery);
}