using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;

internal interface ITestEngineRecognizer
{
    public IList<ITestEngine> RecognizeTestEngines(IDotnetType type);
}
