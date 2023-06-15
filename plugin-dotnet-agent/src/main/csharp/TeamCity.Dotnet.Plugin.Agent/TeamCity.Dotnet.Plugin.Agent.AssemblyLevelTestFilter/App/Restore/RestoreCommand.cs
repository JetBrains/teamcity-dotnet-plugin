using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Restore;

internal class RestoreCommand : Command
{
    [CommandOption(requiresValue: true,"-b", "--backup-metadata")]
    [CommandOptionDescription("File path with metadata about original and changed assemblies to restore (.csv)")]
    [ValidatePath(mustBeFile: false, mustExist: true, errorMessage: "Invalid backup metadata file (.csv)", FileExtension.Csv)]
    public string BackupMetadataFilePath { get; set; } = "backup-metadata.csv";
}