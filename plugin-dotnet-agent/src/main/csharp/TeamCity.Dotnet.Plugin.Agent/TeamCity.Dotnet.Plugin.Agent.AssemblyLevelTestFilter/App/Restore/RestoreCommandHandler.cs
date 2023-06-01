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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Restore;

internal class RestoreCommandHandler : ICommandHandler<RestoreCommand>
{
    private readonly IBackupRestore _backupRestore;
    private readonly ILogger<RestoreCommandHandler> _logger;

    public RestoreCommandHandler(IBackupRestore backupRestore, ILogger<RestoreCommandHandler> logger)
    {
        _backupRestore = backupRestore;
        _logger = logger;
    }

    public async Task ExecuteAsync(RestoreCommand command)
    {
        _logger.LogInformation("Restore command execution started");
        
        var backupMetadataFilePath = Path.GetFullPath(command.BackupMetadataFilePath);
        _logger.LogInformation("Restoring assemblies by metadata from the file {BackupMetadataFilePath}", backupMetadataFilePath);
        
        await _backupRestore.RestoreAsync(backupMetadataFilePath);
        
        _logger.LogDebug("Restore command execution completed");
    }
}