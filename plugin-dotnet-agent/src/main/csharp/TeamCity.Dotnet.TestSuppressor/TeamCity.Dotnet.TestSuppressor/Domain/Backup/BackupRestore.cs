using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Backup;

internal class BackupRestore : IBackupRestore
{
    private readonly IFileSystem _fileSystem;
    private readonly IFileReader _fileReader;
    private readonly ILogger<BackupRestore> _logger;

    public BackupRestore(
        IFileSystem fileSystem,
        IFileReader fileReader,
        ILogger<BackupRestore> logger)
    {
        _fileSystem = fileSystem;
        _fileReader = fileReader;
        _logger = logger;
    }

    public async Task RestoreAsync(string csvFilePath)
    {
        if (!_fileSystem.File.Exists(csvFilePath))
        {
            _logger.LogError("Backup metadata file {BackupCsvPath} doesn't exits", csvFilePath);
            return;
        }
        
        // read the csv file line by line
        await foreach (var (line, lineNumber) in _fileReader.ReadLinesAsync(csvFilePath))
        {
            var metadataEntryResult = BackupFileMetadata.FromString(line);
            if (metadataEntryResult.IsError)
            {
                _logger.LogWarning("Invalid line {LineNumber}: {Reason}", lineNumber, metadataEntryResult.ErrorValue);
                continue;
            }

            var metadata = metadataEntryResult.Value;
            
            _logger.LogDebug("Restoring {BackupFilePath} -> {OriginalFilePath}", metadata.BackupPath, metadata.OriginalPath);
            
            if (!_fileSystem.File.Exists(metadata.BackupPath))
            {
                _logger.LogWarning("Backup file not found: {BackupFilePath}", metadata.BackupPath);
                continue;
            }
            
            try
            {
                // delete the file by original path
                if (!_fileSystem.File.Exists(metadata.OriginalPath))
                {
                    _logger.LogWarning("Original file path not found: {OriginalFilePath}", metadata.OriginalPath);
                }
                else
                {
                    try
                    {
                        _fileSystem.File.Delete(metadata.OriginalPath);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError("Error during the file {OriginalFilePath} removing: {Message}", metadata.OriginalPath, ex.Message);
                    }
                }

                // rename the backup file to the original one
                _fileSystem.File.Move(metadata.BackupPath, metadata.OriginalPath);
                _logger.LogInformation("File {BackupFilePath} restored to {OriginalFilePath}", metadata.BackupPath, metadata.OriginalPath);
            }
            catch (Exception ex)
            {
                _logger.LogError(
                    "Error during replacing {BackupFilePath} -> {OriginalFilePath}: {Message}",
                    metadata.BackupPath,
                    metadata.OriginalPath,
                    ex.Message
                );
            }
        }

        // remove the backup metadata CSV file
        try
        {
            _fileSystem.File.Delete(csvFilePath);
            _logger.LogInformation("Backup metadata file {BackupCsvPath} removed", csvFilePath);
        }
        catch (Exception ex)
        {
            _logger.LogError("Error during backup metadata file removing after restoring: {Message}", ex.Message);
        }
    }
}
