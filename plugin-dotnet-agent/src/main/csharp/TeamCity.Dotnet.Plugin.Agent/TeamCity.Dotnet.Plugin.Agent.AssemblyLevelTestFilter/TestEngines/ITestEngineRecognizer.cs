using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines;

internal interface ITestEngineRecognizer
{
    public IList<ITestEngine> RecognizeTestEngines(TypeDefinition type);
}
