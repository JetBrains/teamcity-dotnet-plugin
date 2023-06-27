using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Domain.Backup;
using TeamCity.Dotnet.TestSuppressor.Domain.Patching;
using TeamCity.Dotnet.TestSuppressor.Domain.Targeting;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine;

namespace TeamCity.Dotnet.TestSuppressor.App.Suppress;

internal class SuppressCommandHandler : ICommandHandler<SuppressCommand>
{
    private readonly ITargetResolver _targetResolver;
    private readonly ITestSelectorsLoader _testSelectorsLoader;
    private readonly IAssemblyPatcher _assemblyPatcher;
    private readonly IBackupMetadataSaver _backupMetadataSaver;
    private readonly ILogger<SuppressCommandHandler> _logger;

    public SuppressCommandHandler(
        ITargetResolver targetResolver,
        ITestSelectorsLoader testSelectorsLoader,
        IAssemblyPatcher assemblyPatcher,
        IBackupMetadataSaver backupMetadataSaver,
        ILogger<SuppressCommandHandler> logger)
    {
        _targetResolver = targetResolver;
        _testSelectorsLoader = testSelectorsLoader;
        _assemblyPatcher = assemblyPatcher;
        _backupMetadataSaver = backupMetadataSaver;
        _logger = logger;
    }
    
    public async Task ExecuteAsync(SuppressCommand command)
    {
        _logger.LogInformation("Suppress command execution started");
        
        var patchingCriteria = new TestSuppressionPatchingCriteria(
            TestSelectors: await _testSelectorsLoader.LoadTestSelectorsFromAsync(command.TestsFilePath),
            InclusionMode: command.InclusionMode
        );

        _logger.LogDebug("Patching criteria created: {PatchingCriteria}", patchingCriteria);

        var patchedAssembliesCounter = 0;
        foreach (var assembly in _targetResolver.Resolve(command.Target))
        {
            _logger.LogDebug("Trying to patch assembly: {Assembly}", assembly);
            
            var patchingResult = await _assemblyPatcher.TryPatchAsync(assembly, patchingCriteria);
            if (patchingResult.IsAssemblyPatched)
            {
                _logger.LogInformation("Assembly patched successfully: {AssemblyPath}", patchingResult.AssemblyPath);
                patchedAssembliesCounter++;

                await SaveBackupMetadata(command, patchingResult);
                _logger.LogDebug("Backup metadata saved for {PatchingResult}", patchingResult);
            }
            else
            {
                _logger.LogDebug("Assembly not patched: {TargetAssembly}", assembly);
            }
        }
        
        _logger.LogInformation("Patching finished: {PatchedAssembliesCounter} assemblies patched", patchedAssembliesCounter);
        _logger.LogInformation("Suppress command execution completed");
    }

    private async Task SaveBackupMetadata(SuppressCommand command, AssemblyPatchingResult patchingResult)
    {
        await _backupMetadataSaver.SaveAsync(command.BackupFilePath, new BackupFileMetadata(
            OriginalPath: patchingResult.AssemblyPath,
            BackupPath: patchingResult.BackupAssemblyPath
        ));

        if (patchingResult.HasSymbols)
        {
            await _backupMetadataSaver.SaveAsync(command.BackupFilePath, new BackupFileMetadata(
                OriginalPath: patchingResult.SymbolsPath!,
                BackupPath: patchingResult.BackupSymbolsPath!
            ));
        }
    }
}