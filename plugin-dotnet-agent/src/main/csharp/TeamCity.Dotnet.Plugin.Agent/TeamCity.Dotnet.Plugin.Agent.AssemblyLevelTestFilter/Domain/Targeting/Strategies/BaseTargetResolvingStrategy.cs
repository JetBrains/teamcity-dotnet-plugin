using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal abstract class BaseTargetResolvingStrategy : ITargetResolvingStrategy
{
    protected readonly IFileSystem FileSystem;
    private readonly ILogger<BaseTargetResolvingStrategy> _logger;

    protected BaseTargetResolvingStrategy(
        IFileSystem fileSystem,
        ILogger<BaseTargetResolvingStrategy> logger)
    {
        FileSystem = fileSystem;
        _logger = logger;
    }

    public abstract TargetType TargetType { get; }
    
    protected abstract IEnumerable<string> AllowedTargetExtensions { get; }

    public abstract IEnumerable<(IFileSystemInfo, TargetType)> Resolve(string target);

    protected IFileSystemInfo? TryToGetTargetFile(string target)
    {
        var (pathFileSystemInfo, exception) = FileSystem.GetFileSystemInfo(target);
        if (exception != null)
        {
            _logger.Log(LogLevel.Warning, exception,"Can't access to target {TargetType} path: {Target}", TargetType, target);
            return null;
        }
        
        if (pathFileSystemInfo!.IsFile() && AllowedTargetExtensions.All(e => e != pathFileSystemInfo!.Extension))
        {
            _logger.LogWarning(
                "Target file {TargetType} has unsupported extension: {Target}. Supported extensions are: {AllowedExtensions}", 
                TargetType,
                target,
                AllowedTargetExtensions
            );
            return null;
        }

        return pathFileSystemInfo;
    }
}