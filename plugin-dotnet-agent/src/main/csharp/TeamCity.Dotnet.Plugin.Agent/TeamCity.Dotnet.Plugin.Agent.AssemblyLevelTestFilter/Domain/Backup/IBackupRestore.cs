namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;

internal interface IBackupRestore
{
    Task RestoreAsync(string csvFilePath);
}