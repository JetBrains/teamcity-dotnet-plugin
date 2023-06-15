namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

internal interface IDotnetType
{
    public string FullName { get; }
    
    IEnumerable<IDotnetCustomAttribute> CustomAttributes { get; }
    
    IEnumerable<IDotnetMethod> Methods { get; }

    void RemoveCustomAttribute(IDotnetCustomAttribute customAttribute);
}