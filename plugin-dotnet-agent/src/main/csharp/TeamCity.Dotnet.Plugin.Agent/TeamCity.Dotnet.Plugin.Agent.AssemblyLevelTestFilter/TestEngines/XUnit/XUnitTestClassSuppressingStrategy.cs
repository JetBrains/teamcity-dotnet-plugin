using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines.XUnit;

internal class XUnitTestClassSuppressingStrategy : DefaultSuppressingStrategy<XUnit>, ITestSuppressingStrategy<XUnit, TestClassQuery>
{
    public XUnitTestClassSuppressingStrategy(XUnit testEngine) : base(testEngine) {}

    public void SuppressTests(TypeDefinition type, TestClassQuery testsQuery) => RemoveAllTestAttributes(type);
}