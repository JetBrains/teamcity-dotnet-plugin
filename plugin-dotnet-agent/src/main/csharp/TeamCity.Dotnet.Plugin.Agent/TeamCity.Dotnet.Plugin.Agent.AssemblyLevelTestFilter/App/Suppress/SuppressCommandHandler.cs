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

        var testSelectors = await _testSelectorsFactory.LoadFromAsync(command.TestsFilePath);
        var patchingCriteria = new TestSuppressionPatchingCriteria(testSelectors, command.InclusionMode);

        await foreach (var targetAssembly in _targetResolver.ResolveAsync(command.Target))
        {
            var patchingResult = await _assemblyPatcher.TryPatchAsync(targetAssembly, patchingCriteria);
            if (patchingResult.IsAssemblyPatched)
            {
                await SaveBackupMetadata(patchingResult);
            }
        }
    }

    private async Task SaveBackupMetadata(AssemblyPatchingResult patchingResult)
    {
        var backupAssemblyMetadata =
            new BackupAssemblyMetadata(patchingResult.AssemblyPath, patchingResult.BackupPath);
        await _backupMetadataSaver.SaveAsync(backupAssemblyMetadata);
    }
}