using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Targeting;

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

    public IEnumerable<IFileInfo> Resolve(IEnumerable<string> targets)
    {
        var resolvedTargets = new HashSet<string>();
        return targets.SelectMany(t => Resolve(t, resolvedTargets));
    }

    private IEnumerable<IFileInfo> Resolve(string target, ISet<string> resolvedTargets)
    {
        _logger.LogInformation("Resolving target: {Target}", target);
        
        var originalTargetPathResult = _fileSystem.TryGetFileSystemInfo(target);
        if (originalTargetPathResult.IsError)
        {
            _logger.LogError(originalTargetPathResult.ErrorValue, "Target not available: {Target}", target);
            throw new FileNotFoundException($"Target '{target}' not available");
        }

        var originalTargetPath = originalTargetPathResult.Value;

        var supposedTargetType = SpeculateTargetType(originalTargetPath);

        // if target is an assembly, we can process once and return it right away
        if (supposedTargetType == TargetType.Assembly)
        {
            foreach (var (resolvedAssembly, _) in AssemblyStrategy.Resolve(originalTargetPath.FullName))
            {
                MarkTargetAsResolved(resolvedTargets, resolvedAssembly);
                yield return (IFileInfo) resolvedAssembly;
            }
            yield break;
        }

        // if target is not an assembly, resolve all targets in the hierarchy using BFS
        var queue = new Queue<(IFileSystemInfo, TargetType)>();
        
        queue.Enqueue((originalTargetPath, supposedTargetType));
        while (queue.Count != 0)
        {
            var (currentTarget, targetType) = queue.Dequeue();
            if (IsAlreadyResolved(resolvedTargets, currentTarget))
            {
                continue;
            }
            
            if (!_strategies.TryGetValue(targetType, out var strategy))
            {
                _logger.LogError("No target resolution strategy for target type: {TargetType}", targetType);
                continue;
            }

            foreach (var (resolvedTargetFile, resolvedTargetType) in strategy.Resolve(currentTarget.FullName))
            {
                if (resolvedTargetType == TargetType.Assembly)
                {
                    if (IsAlreadyResolved(resolvedTargets, resolvedTargetFile))
                    {
                        continue;
                    }
                    
                    foreach (var (resolvedAssembly, _) in AssemblyStrategy.Resolve(resolvedTargetFile.FullName))
                    {
                        MarkTargetAsResolved(resolvedTargets, resolvedAssembly);
                        yield return (IFileInfo) resolvedAssembly;
                    }
                    continue;
                }
                
                queue.Enqueue((resolvedTargetFile, resolvedTargetType));
            }
            
            MarkTargetAsResolved(resolvedTargets, currentTarget);
        }
    }

    private static TargetType SpeculateTargetType(IFileSystemInfo fileSystemInfo)
    {
        if (fileSystemInfo.IsDirectory())
        {
            return TargetType.Directory;
        }
        
        var extension = fileSystemInfo.Extension.ToLowerInvariant();
        
        if (TargetType.MsBuildBinlog.GetPossibleFileExtension().Contains(extension))
        {
            return TargetType.MsBuildBinlog;
        }
        
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

    private static void MarkTargetAsResolved(ICollection<string> resolvedTargets, IFileSystemInfo resolvedTargetFile)
    {
        resolvedTargets.Add(resolvedTargetFile.FullName);
    }

    private bool IsAlreadyResolved(ICollection<string> resolvedTargets, IFileSystemInfo resolvedTargetFile)
    {
        if (!resolvedTargets.Contains(resolvedTargetFile.FullName))
        {
            return false;
        }
        
        _logger.LogInformation("Skip already resolved target: {Target}", resolvedTargetFile.FullName);
        return true;
    }
}
