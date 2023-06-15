using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

internal interface ITestEngineRecognizer
{
    public IList<ITestEngine> RecognizeTestEngines(IDotnetType type);
}
