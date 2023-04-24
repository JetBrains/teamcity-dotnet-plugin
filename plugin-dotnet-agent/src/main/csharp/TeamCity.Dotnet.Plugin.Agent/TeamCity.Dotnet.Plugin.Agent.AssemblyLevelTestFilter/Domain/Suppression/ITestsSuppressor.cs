using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal interface ITestsSuppressor
{
    public int SuppressTests(TypeDefinition testClass, ITestEngine testEngine, TestSuppressionCriteria suppressionCriteria);
}