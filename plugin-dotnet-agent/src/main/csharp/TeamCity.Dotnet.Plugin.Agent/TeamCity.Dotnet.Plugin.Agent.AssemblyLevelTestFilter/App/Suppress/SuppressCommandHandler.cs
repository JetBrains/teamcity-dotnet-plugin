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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Suppress;

internal class SuppressCommandHandler : ICommandHandler<SuppressCommand>
{
    private readonly IHelpService _helpService;
    private readonly ITargetResolver _targetResolver;
    private readonly ITestSelectorsFactory _testSelectorsFactory;
    private readonly IAssemblyPatcher _assemblyPatcher;
    private readonly IBackupMetadataSaver _backupMetadataSaver;
    private readonly ILogger<SuppressCommandHandler> _logger;

    public SuppressCommandHandler(
        IHelpService helpService,
        ITargetResolver targetResolver,
        ITestSelectorsFactory testSelectorsFactory,
        IAssemblyPatcher assemblyPatcher,
        IBackupMetadataSaver backupMetadataSaver,
        ILogger<SuppressCommandHandler> logger)
    {
        _helpService = helpService;
        _targetResolver = targetResolver;
        _testSelectorsFactory = testSelectorsFactory;
        _assemblyPatcher = assemblyPatcher;
        _backupMetadataSaver = backupMetadataSaver;
        _logger = logger;
    }
    
    public async Task ExecuteAsync(SuppressCommand command)
    {
        if (command.Help)
        {
            _helpService.ShowHelpAsync(command);
            return;
        }

        var patchingCriteria = new TestSuppressionPatchingCriteria
        {
            TestSelectors = await _testSelectorsFactory.LoadFromAsync(command.TestsFilePath),
            InclusionMode = command.InclusionMode
        };

        await foreach (var targetAssembly in _targetResolver.ResolveAsync(command.Target))
        {
            var patchingResult = await _assemblyPatcher.TryPatchAsync(targetAssembly, patchingCriteria);
            if (patchingResult.IsAssemblyPatched)
            {
                await _backupMetadataSaver.SaveAsync(new BackupAssemblyMetadata
                {
                    Path = patchingResult.AssemblyPath,
                    BackupPath = patchingResult.BackupPath
                });
            }
        }
    }
}