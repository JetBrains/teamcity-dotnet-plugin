namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

internal interface IDotnetAssemblyReference
{
    string FullName { get; }
    
    string Name { get; }
}