using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Infrastructure;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.MsBuild;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Targeting.Strategies;

internal class ProjectTargetResolvingStrategy : BaseTargetResolvingStrategy
{
    private readonly ILogger<ProjectTargetResolvingStrategy> _logger;
    
    public override TargetType TargetType => TargetType.Project;
    
    public ProjectTargetResolvingStrategy(
        IFileSystem fileSystem,
        ILogger<ProjectTargetResolvingStrategy> logger) : base(fileSystem, logger)
    {
        _logger = logger;
    }

    protected override IEnumerable<string> AllowedTargetExtensions => new[] { FileExtension.CSharpProject };

    public override IEnumerable<(IFileSystemInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target project: {Target}", target);
        
        var targetPathSystemInfo = TryToGetTargetFile(target);
        if (targetPathSystemInfo == null)
        {
            _logger.LogWarning("Invalid project target: {Target}", target);
            yield break;
        }
        
        var projectFile = (IFileInfo) targetPathSystemInfo;
        
        var outputAssemblyPathsResult = GetOutputAssemblyPaths(projectFile!);
        if (outputAssemblyPathsResult.IsError)
        {
            _logger.LogWarning(
                outputAssemblyPathsResult.ErrorValue,
                "Target project {TargetProject} is invalid: {Reason}",
                projectFile!.FullName,
                outputAssemblyPathsResult.ErrorValue.Message
            );
            yield break;
        }

        foreach (var outputAssemblyPath in outputAssemblyPathsResult.Value)
        {
            var assemblyFileInfoResult = FileSystem.TryGetFileInfo(outputAssemblyPath);
            if (assemblyFileInfoResult.IsError)
            {
                _logger.LogWarning(
                    assemblyFileInfoResult.ErrorValue,
                    "Evaluated target project output file {TargetProjectOutputFile} not found: {Reason}",
                    projectFile!.FullName,
                    assemblyFileInfoResult.ErrorValue.Message
                );
                yield break;
            }
        
            var assemblyFileInfo = assemblyFileInfoResult.Value;
        
            _logger.LogInformation("Resolved assembly by target project: {Assembly}", assemblyFileInfo.FullName);
            yield return (assemblyFileInfo, TargetType.Assembly);
        }
        
        foreach (var msBuildBinlogFile in TryFindMsBuildBinlogFiles(projectFile))
        {
            _logger.LogInformation("Resolved MSBuild .binlog file next to the target project: {MsBuildBinlog}", msBuildBinlogFile.FullName);
            yield return (msBuildBinlogFile, TargetType.MsBuildBinlog);
        }
    }

    private static Result<IEnumerable<string>, Exception> GetOutputAssemblyPaths(IFileInfo projectFile)
    {
        try
        {
            using var project = new MsBuildProject(projectFile.FullName);

            var outputPath = project.OutputPath;
            var projectDefinedTargetFileName = project.TargetFileName;
            var targetFrameworks = project.TargetFrameworks;
            
            if (!targetFrameworks.Any())
            {
                // no target frameworks?..
                return Result<IEnumerable<string>, Exception>.Success(Array.Empty<string>());
            }

            var targetFileName = string.IsNullOrWhiteSpace(projectDefinedTargetFileName)
                ? projectFile.Name.Replace(FileExtension.CSharpProject, FileExtension.Dll)
                : projectDefinedTargetFileName;

            var result = targetFrameworks.Select(tf => Path
                .Combine(projectFile.Directory!.FullName, outputPath, tf, targetFileName)
                .Replace('\\', Path.DirectorySeparatorChar)
                .Replace('/', Path.DirectorySeparatorChar)
            ).ToList();
                
            return Result<IEnumerable<string>, Exception>.Success(result);
        }
        catch (Exception exception)
        {
            return Result<IEnumerable<string>, Exception>.Error(exception);
        }
    }
}