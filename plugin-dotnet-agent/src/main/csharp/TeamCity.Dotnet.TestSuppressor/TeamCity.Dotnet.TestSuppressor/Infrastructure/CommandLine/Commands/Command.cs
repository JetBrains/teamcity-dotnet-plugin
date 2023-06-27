using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

public abstract class Command
{
    [CommandOption(requiresValue: false, "-h", "--help", "-?")]
    [CommandOptionDescription("Display help information")]
    public bool Help { get; set; } = false;
    
    [CommandOption(requiresValue: true,"-v", "--verbosity")]
    [CommandOptionDescription("Verbosity of output. Possible values: q[uiet], min[imal], n[ormal], det[ailed], diag[nostic]")]
    [ValidateEnum(typeof(Verbosity), errorMessage: "Invalid verbosity value. Possible values: q[uiet], min[imal], n[ormal], d[etailed], diag[nostic]")]
    public Verbosity Verbosity { get; set; } = Verbosity.Normal;
    
    /// <summary>
    /// If true, command will be executed; necessary for execution path resolution
    /// </summary>
    public bool IsActive { get; set; } = false;
}