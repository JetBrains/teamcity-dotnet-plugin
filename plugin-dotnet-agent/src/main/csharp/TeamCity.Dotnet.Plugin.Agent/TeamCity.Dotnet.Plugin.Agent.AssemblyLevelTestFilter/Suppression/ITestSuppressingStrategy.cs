using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;

internal interface ITestSuppressingStrategy {}

internal interface ITestSuppressingStrategy<TTestEngine, in TTestsQuery> : ITestSuppressingStrategy
    where TTestEngine : ITestEngine
    where TTestsQuery : ITestsQuery
{
    public void SuppressTests(TypeDefinition type, TTestsQuery testsQuery);
}