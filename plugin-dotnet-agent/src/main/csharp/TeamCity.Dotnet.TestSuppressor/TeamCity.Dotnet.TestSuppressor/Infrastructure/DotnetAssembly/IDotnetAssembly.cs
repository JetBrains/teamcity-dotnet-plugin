namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

internal interface IDotnetAssembly : IDisposable
{
    bool HasSymbols { get; }
    
    IEnumerable<IDotnetAssemblyReference> AssemblyReferences { get; }

    IEnumerable<IDotnetType> Types { get; }
    
    void SaveTo(string filePath, bool withSymbols);
}