using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Targeting.Strategies;

internal class DirectoryTargetResolvingStrategy : BaseTargetResolvingStrategy
{
    private readonly ILogger<DirectoryTargetResolvingStrategy> _logger;


    protected override IEnumerable<string> AllowedTargetExtensions => Array.Empty<string>();

    public DirectoryTargetResolvingStrategy(
        IFileSystem fileSystem,
        ILogger<DirectoryTargetResolvingStrategy> logger) : base(fileSystem, logger)
    {
        _logger = logger;
    }

    public override TargetType TargetType => TargetType.Directory;

    public override IEnumerable<(IFileSystemInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target directory: {Target}", target);
        
        var directoryInfoResult = FileSystem.TryGetDirectoryInfo(target);
        if (directoryInfoResult.IsError)
        {
            _logger.LogError(directoryInfoResult.ErrorValue, "Failed to resolve target directory: {Target}", target);
            yield break;
        }

        var directoryInfo = directoryInfoResult.Value;
        
        var slnFiles = GetFileSearchPattern(TargetType.Solution)
            .SelectMany(sp => directoryInfo.GetFiles(sp, SearchOption.TopDirectoryOnly)).ToList();
        var csprojFiles = GetFileSearchPattern(TargetType.Project)
            .SelectMany(sp => directoryInfo.GetFiles(sp, SearchOption.TopDirectoryOnly)).ToList();

        // not sure how to handle this
        // TODO need to test how `dotnet test` handles this:
        // 1. if there are multiple solutions in the directory
        // 2. if there are multiple projects in the directory
        // 3. if there are both solutions and projects in the directory
        // 4. if there are no solutions or projects in the directory
        if (slnFiles.Count != 0)
        {
            foreach (var slnFile in slnFiles)
            {
                _logger.LogInformation("Resolved solution in target directory: {Solution}", slnFile.FullName);
                yield return (slnFile, TargetType.Solution);
            }
        }
        else if (csprojFiles.Count != 0)
        {
            foreach (var csprojFile in csprojFiles)
            {
                _logger.LogInformation("Resolved project in target directory: {Project}", csprojFile.FullName);
                yield return (csprojFile, TargetType.Project);
            }
        }
        else
        {
            foreach (var fileSearchPattern in GetFileSearchPattern(TargetType.Assembly))
            {
                foreach (var assemblyFile in directoryInfo.GetFiles(fileSearchPattern, SearchOption.AllDirectories))
                {
                    _logger.LogInformation("Resolved assembly in target directory: {Assembly}", assemblyFile.FullName);
                    yield return (assemblyFile, TargetType.Assembly);
                }
            }
        }
        
        foreach (var msBuildBinlogFile in TryFindMsBuildBinlogFiles(directoryInfo))
        {
            _logger.LogInformation("Resolved MSBuild .binlog file in the target directory: {MsBuildBinlog}", msBuildBinlogFile.FullName);
            yield return (msBuildBinlogFile, TargetType.MsBuildBinlog);
        }
    }

    private static IEnumerable<string> GetFileSearchPattern(TargetType targetType) =>
        targetType.GetPossibleFileExtension().Select(ext => $"*{ext}");
}