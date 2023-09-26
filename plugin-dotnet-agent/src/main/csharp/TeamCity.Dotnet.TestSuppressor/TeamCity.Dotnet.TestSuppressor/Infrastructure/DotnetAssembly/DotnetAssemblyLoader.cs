using Microsoft.Extensions.Logging;
using Mono.Cecil;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

internal class DotnetAssemblyLoader : IDotnetAssemblyLoader
{
    private readonly ILogger<DotnetAssemblyLoader> _logger;

    public DotnetAssemblyLoader(ILogger<DotnetAssemblyLoader> logger)
    {
        _logger = logger;
    }
    
    public IDotnetAssembly? LoadAssembly(string assemblyPath, bool withSymbols)
    {
        try
        {
            var assemblyDefinition = AssemblyDefinition.ReadAssembly(assemblyPath, new ReaderParameters
            {
                ReadSymbols = withSymbols, // read debug symbols if available
            });

            return new DotnetAssembly(assemblyDefinition, assemblyPath);
        }
        catch (BadImageFormatException exception)
        {
            _logger.LogWarning(exception, "Can't read assembly definition: {Target}", assemblyPath);
            return null;
        }
    }
}