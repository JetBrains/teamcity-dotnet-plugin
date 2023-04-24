using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Restore;

internal class RestoreCommand : Command
{
    [CommandLineOption("-b", "--backup-metadata")]
    [CommandLineOptionDescription("File path with metadata about original and changed assemblies to restore")]
    [ValidatePath(mustBeFile: false, mustExist: true, errorMessage: "Invalid backup metadata file", ".yaml")]
    public string BackupMetadataFilePath { get; set; } = "backup-metadata.yaml";
}