using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

internal interface ITestEngineRecognizer
{
    public IList<ITestEngine> RecognizeTestEngines(TypeDefinition type);
}
