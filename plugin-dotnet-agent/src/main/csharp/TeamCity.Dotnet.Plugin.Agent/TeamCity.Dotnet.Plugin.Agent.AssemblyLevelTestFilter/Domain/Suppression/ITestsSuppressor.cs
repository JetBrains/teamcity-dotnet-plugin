using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal interface ITestsSuppressor
{
    TestSuppressionResult SuppressTests(IDotnetType type, TestSuppressionParameters parameters);
}