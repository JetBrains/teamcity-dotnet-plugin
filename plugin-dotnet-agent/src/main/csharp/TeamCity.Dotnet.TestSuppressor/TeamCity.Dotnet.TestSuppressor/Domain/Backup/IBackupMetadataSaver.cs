namespace TeamCity.Dotnet.TestSuppressor.Domain.Backup;

internal interface IBackupMetadataSaver
{
    Task SaveAsync(string filePath, BackupFileMetadata backupMetadata);
}