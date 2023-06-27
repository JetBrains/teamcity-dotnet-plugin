using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;

internal interface ITestClassDetector
{
    IEnumerable<TestClass> Detect(IDotnetAssembly assembly);
}