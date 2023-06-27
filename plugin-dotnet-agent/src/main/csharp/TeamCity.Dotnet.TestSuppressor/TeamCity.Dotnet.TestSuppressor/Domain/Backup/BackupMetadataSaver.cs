using System.IO.Abstractions;
using Microsoft.Extensions.Logging;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Backup;

internal class BackupMetadataSaver : IBackupMetadataSaver
{
    private readonly IFileSystem _fileSystem;
    private readonly ILogger<BackupMetadataSaver> _logger;

    public BackupMetadataSaver(IFileSystem fileSystem, ILogger<BackupMetadataSaver> logger)
    {
        _fileSystem = fileSystem;
        _logger = logger;
    }
    
    public async Task SaveAsync(string filePath, BackupFileMetadata backupMetadata)
    {
        filePath = _fileSystem.Path.GetFullPath(filePath);
        
        _logger.LogDebug("Saving backup metadata {BackupMetadata} to the file {FilePath}", backupMetadata, filePath);

        await _fileSystem.File.AppendAllLinesAsync(filePath, new [] { backupMetadata.ToString() });
        
        _logger.LogDebug("Backup metadata {BackupMetadata} saved to the file {FilePath}", backupMetadata, filePath);
    }
}