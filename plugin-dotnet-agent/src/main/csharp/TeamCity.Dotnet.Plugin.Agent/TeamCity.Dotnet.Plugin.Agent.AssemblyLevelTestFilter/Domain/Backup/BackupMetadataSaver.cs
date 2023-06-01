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

internal class BackupMetadataSaver : IBackupMetadataSaver
{
    private readonly ILogger<BackupMetadataSaver> _logger;

    public BackupMetadataSaver(ILogger<BackupMetadataSaver> logger)
    {
        _logger = logger;
    }
    
    public async Task SaveAsync(string filePath, BackupFileMetadata backupMetadata)
    {
        filePath = Path.GetFullPath(filePath);
        
        _logger.LogDebug("Saving backup metadata {BackupMetadata} to the file {FilePath}", backupMetadata, filePath);
        
        await using var streamWriter = new StreamWriter(filePath, append: true);
        await streamWriter.WriteLineAsync($"\"{backupMetadata.BackupPath}\";\"{backupMetadata.Path}\"");
        await streamWriter.FlushAsync();
        
        _logger.LogDebug("Backup metadata {BackupMetadata} saved to the file {FilePath}", backupMetadata, filePath);
    }
}