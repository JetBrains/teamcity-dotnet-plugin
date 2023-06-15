namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

internal interface IDotnetMethod
{
    IEnumerable<IDotnetCustomAttribute> CustomAttributes { get; }
    
    void RemoveCustomAttribute(IDotnetCustomAttribute attribute);
}