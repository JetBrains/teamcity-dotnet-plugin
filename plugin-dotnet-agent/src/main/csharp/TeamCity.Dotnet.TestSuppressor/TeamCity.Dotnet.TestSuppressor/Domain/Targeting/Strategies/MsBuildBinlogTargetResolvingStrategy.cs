using System.IO.Abstractions;
using Microsoft.Build.Framework;
using Microsoft.Build.Logging.StructuredLogger;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Infrastructure;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.MsBuild;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Targeting.Strategies;

/// <summary>
/// Try to parse assemblies from MSBuild .binlog file
/// About binary log: https://github.com/dotnet/msbuild/blob/main/documentation/wiki/Binary-Log.md
/// Binary log parser: https://github.com/KirillOsenkov/MSBuildStructuredLog
/// </summary>
internal class MsBuildBinlogTargetResolvingStrategy : BaseTargetResolvingStrategy
{
    private readonly ILogger<MsBuildBinlogTargetResolvingStrategy> _logger;
    
    public override TargetType TargetType => TargetType.MsBuildBinlog;
    
    public MsBuildBinlogTargetResolvingStrategy(
        IFileSystem fileSystem,
        ILogger<MsBuildBinlogTargetResolvingStrategy> logger) : base(fileSystem, logger)
    {
        _logger = logger;
    }

    protected override IEnumerable<string> AllowedTargetExtensions => new[] { FileExtension.MsBuildBinaryLog };

    public override IEnumerable<(IFileSystemInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target MSBuild .binlog file: {Target}", target);
        
        var targetPathSystemInfo = TryToGetTargetFile(target);
        if (targetPathSystemInfo == null)
        {
            _logger.LogWarning("Invalid MSBuild .binlog target: {Target}", target);
            yield break;
        }
        
        var binlogFile = targetPathSystemInfo as IFileInfo;
        
        var outputAssemblyPathsResult = GetOutputAssemblyPaths(binlogFile!);
        if (outputAssemblyPathsResult.IsError)
        {
            _logger.LogWarning(
                outputAssemblyPathsResult.ErrorValue,
                "Target MSBuild .binlog {TargetProject} is invalid: {Reason}",
                binlogFile!.FullName,
                outputAssemblyPathsResult.ErrorValue.Message
            );
            yield break;
        }
        
        _logger.LogDebug(
            "Resolved {AssembliesCount} assemblies by target MSBuild .binlog file: {Target}",
            outputAssemblyPathsResult.Value.Count(),
            target
        );
        
        foreach (var outputAssemblyPath in outputAssemblyPathsResult.Value)
        {
            var assemblyFileInfoResult = FileSystem.TryGetFileInfo(outputAssemblyPath!);
            if (assemblyFileInfoResult.IsError)
            {
                _logger.LogWarning(
                    assemblyFileInfoResult.ErrorValue,
                    "Target MSBuild .binlog output file {OutputAssemblyPath} does not exist",
                    outputAssemblyPath
                );
                yield break;
            }

            var assemblyFileInfo = assemblyFileInfoResult.Value;
        
            _logger.LogInformation("Resolved assembly by target MSBuild .binlog file: {Assembly}", assemblyFileInfo.FullName);
            yield return (assemblyFileInfo, TargetType.Assembly);
        }
    }

    private Result<IEnumerable<string>, Exception> GetOutputAssemblyPaths(IFileSystemInfo binlogFile)
    {
        try
        {
            var result = new HashSet<string>();
            
            foreach (var record in BinaryLog.ReadRecords(binlogFile.FullName).Where(r => r.Args != null))
            {
                // heuristic #1: analyze Build target outputs to find output assemblies
                if (record.Args is TargetFinishedEventArgs { TargetName: "Build", TargetOutputs: not null } targetArgs)
                {
                    foreach (ITaskItem output in targetArgs.TargetOutputs)
                    {
                        var outputPath = output.ItemSpec.Trim();
                        if (!outputPath.HasTargetFileExtension(TargetType.Assembly))
                        {
                            continue;
                        }

                        _logger.LogDebug("MSBuild .binlog reading: target finished event for target \"Build\" has target output: {OutputPath}", outputPath);
                        result.Add(outputPath);
                    }
                }
                
                // heuristic #2: looking for log entries of adding item with moniker-specific target path
                // useful for .sln/.csproj files with <TargetFrameworks> tag instead of <TargetFramework>;
                // we expect to find items of type `TargetPathWithTargetPlatformMoniker` – their item specs are output assemblies paths
                else if (record.Args.Message != null && record.HasItemOfType("TargetPathWithTargetPlatformMoniker"))
                {
                    foreach (var itemSpec in record.GetItemsSpecs().Select(s => s.Trim()))
                    {
                        if (!itemSpec.HasTargetFileExtension(TargetType.Assembly))
                        {
                            continue;
                        }
                    
                        _logger.LogDebug("MSBuild .binlog reading: found item \"TargetPathWithTargetPlatformMoniker\" with item spec: {OutputPath}", itemSpec);
                        result.Add(itemSpec);
                    }
                }
            }
            
            return Result<IEnumerable<string>, Exception>.Success(result);
        }
        catch (Exception exception)
        {
            return Result<IEnumerable<string>, Exception>.Error(exception);
        }
    }
}