namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

internal interface IDotnetMethod
{
    IEnumerable<IDotnetCustomAttribute> CustomAttributes { get; }
    
    void RemoveCustomAttribute(IDotnetCustomAttribute attribute);
}