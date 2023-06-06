/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;

internal class BackupRestore : IBackupRestore
{
    private readonly IFileSystem _fileSystem;
    private readonly ILogger<BackupRestore> _logger;

    public BackupRestore(IFileSystem fileSystem, ILogger<BackupRestore> logger)
    {
        _fileSystem = fileSystem;
        _logger = logger;
    }

    public async Task RestoreAsync(string csvFilePath)
    {
        if (!_fileSystem.FileExists(csvFilePath))
        {
            _logger.LogError("Backup metadata file {BackupCsvPath} doesn't exits", csvFilePath);
            return;
        }
        
        // read the csv file line by line
        await foreach (var (line, lineNumber) in _fileSystem.ReadLinesAsync(csvFilePath))
        {
            var paths = line.Split(';').Select(s => s.Trim('"')).ToArray();
            if (paths.Length != 2)
            {
                _logger.LogInformation("Invalid line {LineNumber}: `{Line}`", lineNumber, line);
                continue;
            }

            var (backupFilePath, originalFilePath) = (paths[0], paths[1]);
            
            _logger.LogDebug("Restoring {BackupFilePath} -> {OriginalFilePath}", backupFilePath, originalFilePath);
            
            if (!_fileSystem.FileExists(backupFilePath))
            {
                _logger.LogWarning("Backup file not found: {BackupFilePath}", backupFilePath);
                continue;
            }
            
            try
            {
                // delete the original file
                if (!_fileSystem.FileExists(originalFilePath))
                {
                    _logger.LogWarning("Original file not found: {OriginalFilePath}", originalFilePath);
                }
                else
                {
                    try
                    {
                        _fileSystem.FileDelete(originalFilePath);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError("Error during the file {OriginalFilePath} removing: {Message}", originalFilePath, ex.Message);
                    }
                }

                // rename the backup file to the original one
                _fileSystem.FileMove(backupFilePath, originalFilePath);
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
            _fileSystem.FileDelete(csvFilePath);
            _logger.LogError("Backup metadata file {BackupCsvPath} removed", csvFilePath);
        }
        catch (Exception ex)
        {
            _logger.LogError("Error during backup metadata file removing after restoring: {Message}", ex.Message);
        }
    }
}
