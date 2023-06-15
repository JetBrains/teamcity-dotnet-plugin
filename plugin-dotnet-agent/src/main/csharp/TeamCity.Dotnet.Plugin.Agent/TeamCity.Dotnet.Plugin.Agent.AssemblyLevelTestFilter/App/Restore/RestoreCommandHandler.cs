using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Restore;

internal class RestoreCommandHandler : ICommandHandler<RestoreCommand>
{
    private readonly IBackupRestore _backupRestore;
    private readonly IFileSystem _fileSystem;
    private readonly ILogger<RestoreCommandHandler> _logger;

    public RestoreCommandHandler(
        IBackupRestore backupRestore,
        IFileSystem fileSystem,
        ILogger<RestoreCommandHandler> logger)
    {
        _backupRestore = backupRestore;
        _fileSystem = fileSystem;
        _logger = logger;
    }

    public async Task ExecuteAsync(RestoreCommand command)
    {
        _logger.LogInformation("Restore command execution started");
        
        var backupMetadataFilePath = _fileSystem.Path.GetFullPath(command.BackupMetadataFilePath);
        _logger.LogInformation("Restoring assemblies by metadata from the file {BackupMetadataFilePath}", backupMetadataFilePath);
        
        await _backupRestore.RestoreAsync(backupMetadataFilePath);
        
        _logger.LogDebug("Restore command execution completed");
    }
}