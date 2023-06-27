namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

internal interface IDotnetType
{
    public string FullName { get; }
    
    IEnumerable<IDotnetCustomAttribute> CustomAttributes { get; }
    
    IEnumerable<IDotnetMethod> Methods { get; }

    void RemoveCustomAttribute(IDotnetCustomAttribute customAttribute);
}