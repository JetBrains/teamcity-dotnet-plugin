using System.IO.Abstractions;
using Microsoft.Build.Construction;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Targeting.Strategies;

internal class SolutionTargetResolvingStrategy : BaseTargetResolvingStrategy
{
    private readonly ILogger<SolutionTargetResolvingStrategy> _logger;
    
    public override TargetType TargetType => TargetType.Solution;

    public SolutionTargetResolvingStrategy(
        IFileSystem fileSystem,
        ILogger<SolutionTargetResolvingStrategy> logger) : base(fileSystem, logger)
    {
        _logger = logger;
    }

    protected override IEnumerable<string> AllowedTargetExtensions => new []{ FileExtension.Solution, FileExtension.SolutionFilter };

    public override IEnumerable<(IFileSystemInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target solution: {Target}", target);

        var solutionFile = TryToGetTargetFile(target);
        if (solutionFile == null)
        {
            _logger.LogWarning("Invalid solution target: {Target}", target);
            yield break;
        }

        var (solution, solutionParsingException) = ParseSolution(solutionFile.FullName);
        if (solutionParsingException != null)
        {
            _logger.LogWarning(solutionParsingException,"Target solution {TargetProject} is invalid", solutionFile.FullName);
            yield break;
        }

        foreach (var project in solution!.ProjectsInOrder)
        {
            if (project.ProjectType != SolutionProjectType.KnownToBeMSBuildFormat)
            {
                _logger.LogDebug("Skipping project of unknown type: {Project}", project.AbsolutePath);
                continue;
            }

            var projectFile = FileSystem.FileInfo.New(project.AbsolutePath);
            _logger.LogInformation("Resolved project by target solution: {Project}", projectFile.FullName);
            
            yield return (projectFile, TargetType.Project);
        }
        
        foreach (var msBuildBinlogFile in TryFindMsBuildBinlogFiles(solutionFile))
        {
            _logger.LogInformation("Resolved MSBuild .binlog file next to the target solution: {MsBuildBinlog}", msBuildBinlogFile.FullName);
            yield return (msBuildBinlogFile, TargetType.MsBuildBinlog);
        }
    }
    
    private static (SolutionFile?, Exception?) ParseSolution(string solutionFilePath)
    {
        try
        {
            return (SolutionFile.Parse(solutionFilePath), null);
        } catch (Exception exception)
        {
            return (null, exception);
        }
    }
}