namespace TeamCity.Dotnet.TestSuppressor.Domain.Backup;

internal interface IBackupRestore
{
    Task RestoreAsync(string csvFilePath);
}