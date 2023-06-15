namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;

internal interface IBackupMetadataSaver
{
    Task SaveAsync(string filePath, BackupFileMetadata backupMetadata);
}