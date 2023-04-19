using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines.NUnit;

internal class NUnitTestClassSuppressingStrategy : DefaultSuppressingStrategy<NUnit>, ITestSuppressingStrategy<NUnit, TestClassQuery>
{
    public NUnitTestClassSuppressingStrategy(NUnit testEngine) : base(testEngine) {}

    public void SuppressTests(TypeDefinition type, TestClassQuery testsQuery) => RemoveAllTestAttributes(type);
}