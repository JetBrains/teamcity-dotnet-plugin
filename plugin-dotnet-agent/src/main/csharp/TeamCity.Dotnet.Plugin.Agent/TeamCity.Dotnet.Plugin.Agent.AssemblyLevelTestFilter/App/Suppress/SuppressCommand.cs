using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FileSystemExtensions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infxrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Suppress;

internal class SuppressCommand : Command
{
    [CommandOption(requiresValue: true,"-t", "--target")]
    [CommandOptionDescription("Path to target. It could be directory, .sln, .csproj, .dll or .exe")]
    [Required(errorMessage: "Target path is required and can't be empty")]
    [ValidatePath(mustBeFile: false, mustExist: true, errorMessage: "Invalid target path", FileExtension.Solution, FileExtension.CSharpProject, FileExtension.Dll, FileExtension.Exe)]
    public string Target { get; set; } = string.Empty;

    [CommandOption(requiresValue: true, "-l", "--test-list")]
    [CommandOptionDescription("Path to file with tests selectors list")]
    [Required(errorMessage: "Tests selectors file path is required and can't be empty")]
    [ValidatePath(mustBeFile: true, mustExist: true, errorMessage: "Invalid tests selectors file path", FileExtension.Txt)]
    public string TestsFilePath { get; set; } = string.Empty;
    
    [CommandOption(requiresValue: false,"-i", "--inclusion-mode")]
    [CommandOptionDescription("Inclusion mode. If true, only tests from the file will be executed. Otherwise, all tests except those from the file will be executed")]
    public bool InclusionMode { get; set; } = false;
    
    [CommandOption(requiresValue: true,"-b", "--backup")]
    [CommandOptionDescription("Backup original assemblies metadata file path (.csv)")]
    [ValidatePath(mustBeFile: true, mustExist: false, errorMessage: "Invalid backup file path. Should be .csv", FileExtension.Csv)]
    public string BackupFilePath { get; set; } = "backup-metadata.csv";
}