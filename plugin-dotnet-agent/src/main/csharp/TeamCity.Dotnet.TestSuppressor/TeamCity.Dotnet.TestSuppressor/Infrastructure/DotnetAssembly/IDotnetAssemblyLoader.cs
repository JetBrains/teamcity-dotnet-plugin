namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

internal interface IDotnetAssemblyLoader
{
    IDotnetAssembly? LoadAssembly(string assemblyPath, bool withSymbols);
}