using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;

internal record TestClass(IDotnetType Type, IList<ITestEngine> TestEngines);