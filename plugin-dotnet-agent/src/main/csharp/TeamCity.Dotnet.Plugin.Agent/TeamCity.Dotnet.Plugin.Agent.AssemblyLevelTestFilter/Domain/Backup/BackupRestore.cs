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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;

internal class BackupRestore : IBackupRestore
{
    private readonly ILogger<BackupRestore> _logger;

    public BackupRestore(ILogger<BackupRestore> logger)
    {
        _logger = logger;
    }

    public async Task RestoreAsync(string csvFilePath)
    {
        // read the csv file line by line
        using (var file = new StreamReader(csvFilePath))
        {
            var lineNumber = 0;
            while (await file.ReadLineAsync() is { } line)
            {
                lineNumber++;

                var paths = line.Split(';').Select(s => s.Trim('"')).ToArray();
                if (paths.Length != 2)
                {
                    _logger.LogInformation("Invalid line {LineNumber}: `{Line}`", lineNumber, line);
                    continue;
                }

                var (backupFilePath, originalFilePath) = (paths[0], paths[1]);
                
                _logger.LogDebug("Restoring {BackupFilePath} -> {OriginalFilePath}", backupFilePath, originalFilePath);
                
                if (!File.Exists(backupFilePath))
                {
                    _logger.LogWarning("Backup file not found: {BackupFilePath}", backupFilePath);
                    continue;
                }
                
                try
                {
                    // delete the original file
                    if (!File.Exists(originalFilePath))
                    {
                        _logger.LogWarning("Original file not found: {OriginalFilePath}", originalFilePath);
                    }
                    else
                    {
                        try
                        {
                            File.Delete(originalFilePath);
                        }
                        catch (Exception ex)
                        {
                            _logger.LogError("Error during the file {OriginalFilePath} removing: {Message}", originalFilePath, ex.Message);
                        }
                    }

                    // rename the backup file to the original one
                    File.Move(backupFilePath, originalFilePath);
                    _logger.LogInformation("File {BackupFilePath} restored to {OriginalFilePath}", backupFilePath,
                        originalFilePath);
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
        }

        // remove the backup metadata CSV file
        try
        {
            File.Delete(csvFilePath);
        }
        catch (Exception ex)
        {
            _logger.LogError("Error during CSV file removing: {Message}", ex.Message);
        }
    }
}
