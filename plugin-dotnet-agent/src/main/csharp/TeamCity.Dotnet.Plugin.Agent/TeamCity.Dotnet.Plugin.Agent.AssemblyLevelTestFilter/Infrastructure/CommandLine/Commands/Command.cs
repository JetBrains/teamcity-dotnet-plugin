using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

internal abstract class Command
{
    [CommandLineOption("-h", "--help", "-?")]
    [CommandLineOptionDescription("Display help information")]
    public bool Help { get; set; } = false;
    
    [CommandLineOption("-v", "--verbosity")]
    [CommandLineOptionDescription("Verbosity of output. Possible values: quiet, minimal, normal, detailed, diagnostic")]
    [ValidateEnum(typeof(Verbosity), errorMessage: "Invalid verbosity value. Possible values: quiet, minimal, normal, detailed, diagnostic")]
    public Verbosity Verbosity { get; set; } = Verbosity.Normal;
}