using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.TestSuppressor.App.Restore;

internal class RestoreCommand : Command
{
    [CommandOption(requiresValue: true,"-b", "--backup-metadata")]
    [CommandOptionDescription("File path with metadata about original and changed assemblies to restore (.csv)")]
    [ValidatePath(mustBeFile: true, mustExist: true, errorMessage: "Invalid backup metadata file (.csv)", FileExtension.Csv)]
    public string BackupMetadataFilePath { get; set; } = "backup-metadata.csv";
}