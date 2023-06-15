using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

internal class DotnetAssembly : IDotnetAssembly
{
    private readonly AssemblyDefinition _assemblyDefinition;

    public DotnetAssembly(AssemblyDefinition assemblyDefinition)
    {
        _assemblyDefinition = assemblyDefinition;
    }

    public bool HasSymbols => _assemblyDefinition.MainModule.HasSymbols;

    public IEnumerable<IDotnetAssemblyReference> AssemblyReferences =>
        _assemblyDefinition.MainModule.AssemblyReferences.Select(ar => new DotnetAssemblyReference(ar));

    public void SaveTo(string filePath, bool withSymbols)
    {
        using var destinationFileStream = File.Create(filePath);
        _assemblyDefinition.Write(destinationFileStream, new WriterParameters { WriteSymbols = withSymbols });
    }

    public IEnumerable<IDotnetType> Types => _assemblyDefinition.Modules
        .SelectMany(module => module.Types)
        .Select(t => new DotnetType(t));

    public void Dispose()
    {
        _assemblyDefinition.Dispose();
    }
}