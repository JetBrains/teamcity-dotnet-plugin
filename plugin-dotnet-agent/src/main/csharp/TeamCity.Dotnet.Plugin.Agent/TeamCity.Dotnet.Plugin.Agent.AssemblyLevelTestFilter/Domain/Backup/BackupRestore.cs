using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;

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
            var paths = line.Split(';').Select(s => s.Trim('"')).ToArray();
            if (paths.Length != 2)
            {
                _logger.LogInformation("Invalid line {LineNumber}: `{Line}`", lineNumber, line);
                continue;
            }

            var (backupFilePath, originalFilePath) = (paths[0], paths[1]);
            
            _logger.LogDebug("Restoring {BackupFilePath} -> {OriginalFilePath}", backupFilePath, originalFilePath);
            
            if (!_fileSystem.File.Exists(backupFilePath))
            {
                _logger.LogWarning("Backup file not found: {BackupFilePath}", backupFilePath);
                continue;
            }
            
            try
            {
                // delete the original file
                if (!_fileSystem.File.Exists(originalFilePath))
                {
                    _logger.LogWarning("Original file not found: {OriginalFilePath}", originalFilePath);
                }
                else
                {
                    try
                    {
                        _fileSystem.File.Delete(originalFilePath);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError("Error during the file {OriginalFilePath} removing: {Message}", originalFilePath, ex.Message);
                    }
                }

                // rename the backup file to the original one
                _fileSystem.File.Move(backupFilePath, originalFilePath);
                _logger.LogInformation("File {BackupFilePath} restored to {OriginalFilePath}", backupFilePath, originalFilePath);
            }
            catch (Exception ex)
            {
                _logger.LogError(
                    "Error during replacing {BackupFilePath} -> {OriginalFilePath}: {Message}",
                    backupFilePath,
                    originalFilePath,
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
