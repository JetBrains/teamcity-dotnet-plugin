namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

internal interface IDotnetCustomAttribute
{
    string FullName { get; }
}