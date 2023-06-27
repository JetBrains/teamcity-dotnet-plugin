namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

internal interface IDotnetAssemblyReference
{
    string FullName { get; }
    
    string Name { get; }
}