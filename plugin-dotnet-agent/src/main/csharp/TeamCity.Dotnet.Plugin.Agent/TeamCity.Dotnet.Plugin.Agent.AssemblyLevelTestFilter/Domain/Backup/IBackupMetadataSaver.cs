namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;

internal interface IBackupMetadataSaver
{
    Task SaveAsync(BackupAssemblyMetadata backupMetadata);
}