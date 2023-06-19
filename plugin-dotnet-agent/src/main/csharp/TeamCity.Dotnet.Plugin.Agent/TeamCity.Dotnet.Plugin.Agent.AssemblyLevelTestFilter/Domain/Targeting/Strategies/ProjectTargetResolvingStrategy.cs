using System.IO.Abstractions;
using Microsoft.Build.Evaluation;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FileSystemExtensions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.MsBuild;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infxrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class ProjectTargetResolvingStrategy : BaseTargetResolvingStrategy, ITargetResolvingStrategy
{
    private readonly ILogger<ProjectTargetResolvingStrategy> _logger;
    
    public override TargetType TargetType => TargetType.Project;
    
    public ProjectTargetResolvingStrategy(
        IFileSystem fileSystem,
        IMsBuildLocator msBuildLocator,
        ILogger<ProjectTargetResolvingStrategy> logger) : base(fileSystem, logger)
    {
        _logger = logger;
        _ = msBuildLocator.RegisterDefaults();
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
        
        var projectFile = targetPathSystemInfo as IFileInfo;
        
        var (outputAssemblyPath, projectParsingException) = GetOutputAssemblyPath(projectFile!);
        if (projectParsingException != null)
        {
            _logger.LogWarning(projectParsingException,"Target project {TargetProject} is invalid", projectFile!.FullName);
            yield break;
        }
        
        var assemblyFileInfoResult = FileSystem.TryGetFileInfo(outputAssemblyPath!);
        if (assemblyFileInfoResult.IsError)
        {
            _logger.LogWarning(assemblyFileInfoResult.Exception, "Target project output file {TargetProjectOutputFile} does not exist", projectFile!.FullName);
            yield break;
        }

        var assemblyFileInfo = assemblyFileInfoResult.Value;
        
        _logger.LogInformation("Resolved assembly by target project: {Assembly}", assemblyFileInfo.FullName);
        yield return (assemblyFileInfo, TargetType.Assembly);
    }

    private static (string?, Exception?) GetOutputAssemblyPath(IFileInfo projectFile)
    {
        try
        {
            var project = new Project(projectFile.FullName);

            // TODO: currently we support only default output path and default target file name
            var outputPath = project.GetPropertyValue("OutputPath");
            var targetFileName =
                project.GetPropertyValue("TargetFileName")
                ?? projectFile.Name.Replace(FileExtension.CSharpProject, string.Empty);

            var result = Path
                .Combine(projectFile.Directory!.FullName, outputPath, targetFileName)
                .Replace('\\', Path.DirectorySeparatorChar)
                .Replace('/', Path.DirectorySeparatorChar);
            
            return (result, null);
        } catch (Exception exception)
        {
            return (null, exception);
        }
    }
}