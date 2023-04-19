using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines.MsTest;

internal class MsTestTestClassSuppressingStrategy : DefaultSuppressingStrategy<MsTest>, ITestSuppressingStrategy<MsTest, TestClassQuery>
{
    public MsTestTestClassSuppressingStrategy(MsTest testEngine) : base(testEngine) {}

    public void SuppressTests(TypeDefinition type, TestClassQuery testsQuery) => RemoveAllTestAttributes(type);
}