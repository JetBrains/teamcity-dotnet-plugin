namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

internal interface IDotnetAssembly : IDisposable
{
    string FullPath { get; }
    
    bool HasSymbols { get; }
    
    IEnumerable<IDotnetAssemblyReference> AssemblyReferences { get; }

    IEnumerable<IDotnetType> Types { get; }
    
    void SaveTo(string filePath, bool withSymbols);
}