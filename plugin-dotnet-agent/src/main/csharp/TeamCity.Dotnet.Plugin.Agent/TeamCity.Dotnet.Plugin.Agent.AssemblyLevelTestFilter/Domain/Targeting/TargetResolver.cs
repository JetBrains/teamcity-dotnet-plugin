using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal class TargetResolver : ITargetResolver
{
    private readonly IDictionary<TargetType, ITargetResolvingStrategy> _strategies;
    private readonly IFileSystem _fileSystem;
    private readonly ILogger<TargetResolver> _logger;

    public TargetResolver(
        IEnumerable<ITargetResolvingStrategy> strategies,
        IFileSystem fileSystem,
        ILogger<TargetResolver> logger)
    {
        _strategies = strategies.ToDictionary(s => s.TargetType);
        _fileSystem = fileSystem;
        _logger = logger;
    }
    
    private ITargetResolvingStrategy AssemblyStrategy => _strategies[TargetType.Assembly];

    public IEnumerable<IFileInfo> Resolve(string target)
    {
        _logger.LogInformation("Resolving target: {Target}", target);

        var originalTargetPathResult = _fileSystem.TryGetFileSystemInfo(target);
        if (originalTargetPathResult.IsError)
        {
            _logger.LogError(originalTargetPathResult.Exception, "Target not available: {Target}", target);
            throw new FileNotFoundException($"Target '{target}' not available");
        }

        var originalTargetPath = originalTargetPathResult.Value;
        
        var supposedTargetType = SpeculateTargetType(originalTargetPath);
        
        // if target is an assembly, we can process once and return it right away
        if (supposedTargetType == TargetType.Assembly)
        {
            foreach (var (resolvedAssembly, _) in AssemblyStrategy.Resolve(originalTargetPath.FullName))
            {
                yield return (IFileInfo) resolvedAssembly;
            }
            yield break;
        }

        // if target is not an assembly, resolve all targets in the hierarchy using BFS
        var queue = new Queue<(IFileSystemInfo, TargetType)>();
        
        queue.Enqueue((originalTargetPath, supposedTargetType));
        while (queue.Count != 0)
        {
            var (currentFileSystemInfo, targetType) = queue.Dequeue();
            if (!_strategies.TryGetValue(targetType, out var strategy))
            {
                _logger.LogError("No target resolution strategy for target type: {TargetType}", targetType);
                continue;
            }
            
            foreach (var (resolvedTargetFile, resolvedTargetType) in strategy.Resolve(currentFileSystemInfo.FullName))
            {
                if (resolvedTargetType == TargetType.Assembly)
                {
                    foreach (var (resolvedAssembly, _) in AssemblyStrategy.Resolve(resolvedTargetFile.FullName))
                    {
                        yield return (IFileInfo) resolvedAssembly;
                    }
                    continue;
                }
                
                queue.Enqueue((resolvedTargetFile, resolvedTargetType));
            }
        }
    }
    
    private  TargetType SpeculateTargetType(IFileSystemInfo fileSystemInfo)
    {
        if (fileSystemInfo.IsDirectory())
        {
            return TargetType.Directory;
        }
        
        var extension = fileSystemInfo.Extension.ToLowerInvariant();
        
        if (TargetType.Assembly.GetPossibleFileExtension().Contains(extension))
        {
            return TargetType.Assembly;
        }

        if (TargetType.Project.GetPossibleFileExtension().Contains(extension))
        {
            return TargetType.Project;
        }

        if (TargetType.Solution.GetPossibleFileExtension().Contains(extension))
        {
            return TargetType.Solution;
        }

        throw new NotSupportedException($"Unsupported target type: '{extension}'.");
    }
}
