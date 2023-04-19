using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;

internal interface ITestsSuppressor
{
    public void SuppressTests(TypeDefinition testClass, ITestEngine testEngine, TestSuppressionCriterion suppressionCriterion);
}