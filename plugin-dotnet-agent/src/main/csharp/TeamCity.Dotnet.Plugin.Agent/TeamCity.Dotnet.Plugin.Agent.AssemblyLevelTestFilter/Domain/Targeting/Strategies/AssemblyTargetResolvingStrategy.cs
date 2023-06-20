using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infxrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class AssemblyTargetResolvingStrategy : BaseTargetResolvingStrategy
{
    private readonly ILogger<AssemblyTargetResolvingStrategy> _logger;
    private readonly IDotnetAssemblyLoader _assemblyLoader;
    private readonly IReadOnlyList<ITestEngine> _testEngines;

    public override TargetType TargetType => TargetType.Assembly;

    public AssemblyTargetResolvingStrategy(
        IFileSystem fileSystem,
        ILogger<AssemblyTargetResolvingStrategy> logger,
        IEnumerable<ITestEngine> testEngines,
        IDotnetAssemblyLoader assemblyLoader) : base(fileSystem, logger)
    {
        _logger = logger;
        _assemblyLoader = assemblyLoader;
        _testEngines = testEngines.ToList();
    }

    protected override IEnumerable<string> AllowedTargetExtensions => new [] { FileExtension.Dll, FileExtension.Exe };

    public override IEnumerable<(IFileSystemInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target assembly: {Target}", target);

        var assemblyFile = TryToGetTargetFile(target);
        if (assemblyFile == null)
        {
            _logger.LogWarning("Invalid assembly target file: {Target}", target);
            yield break;
        }

        var (isAssembly, detectedEngines) = TryDetectEngines(assemblyFile);
        if (!isAssembly)
        {
            _logger.LogDebug("Target assembly is not a .NET assembly: {Target}", target);
            yield break;
        }
        if (detectedEngines == null || detectedEngines.Length == 0)
        {
            _logger.LogDebug("Target assembly doesn't contain tests written on supported test frameworks: {Target}", target);
            yield break;
        }
        
        _logger.LogInformation("Assembly {Target} depends on following test frameworks: {Engines}", target, string.Join(", ", detectedEngines));
        _logger.LogInformation("Resolved assembly: {Assembly}", assemblyFile.FullName);
        yield return (assemblyFile, TargetType.Assembly);
    }

    private (bool isAssembly, string[]? detectedEngines) TryDetectEngines(IFileSystemInfo assemblyFile)
    {
        using var assembly = _assemblyLoader.LoadAssembly(assemblyFile.FullName, false);
        if (assembly == null)
        {
            return (false, null);
        }
        
        var assemblyReferences = assembly.AssemblyReferences.ToList();
        if (!assemblyReferences.Any())
        {
            return (true, null);
        }
        
        _logger.LogDebug("Examine assembly {Assembly} references:\n\t\t\t\t{AssemblyAttrs}", 
            assemblyFile, string.Join("\n\t\t\t\t", assemblyReferences.Select(a => a.FullName)));

        var detectedEngines = _testEngines
            .Where(te => te.AssemblyNames.Any(tca => assemblyReferences.Any(a => a.Name == tca)))
            .Select(te => te.Name)
            .ToArray();
        return (true, detectedEngines);
    }
}