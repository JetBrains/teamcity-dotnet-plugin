namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

internal interface IDotnetAssemblyLoader
{
    IDotnetAssembly? LoadAssembly(string assemblyPath, bool withSymbols);
}