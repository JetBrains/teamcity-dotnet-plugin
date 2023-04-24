using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Suppress;

internal class SuppressCommand : Command
{
    [CommandLineOption("-t", "--target")]
    [CommandLineOptionDescription("Path to target. It could be directory, .sln, .csproj or .dll")]
    [Required(errorMessage: "Target path is required")]
    [ValidatePath(mustBeFile: false, mustExist: true, errorMessage: "Invalid target path", ".sln", ".csproj", ".dll")]
    public string Target { get; set; } = string.Empty;

    [CommandLineOption("-tl", "--tests-list")]
    [CommandLineOptionDescription("Path to file with test (classes) names")]
    [Required(errorMessage: "Tests (classes) file path is required")]
    [ValidatePath(mustBeFile: true, mustExist: true, errorMessage: "Invalid tests (classes) file path", ".txt")]
    public string TestsFilePath { get; set; } = "include.txt";
    
    [CommandLineOption("-im", "--inclusion-mode")]
    [CommandLineOptionDescription("Inclusion mode. If true, only tests from the file will be executed. Otherwise, all tests except those from the file will be executed")]
    public bool InclusionMode { get; set; } = false;
    
    [CommandLineOption("-b", "--backup")]
    [CommandLineOptionDescription("Backup original assemblies metadata file path")]
    [ValidatePath(mustBeFile: true, mustExist: false, errorMessage: "Invalid backup file path", ".yaml")]
    public string BackupFilePath { get; set; } = "backup-metadata.yaml";
}