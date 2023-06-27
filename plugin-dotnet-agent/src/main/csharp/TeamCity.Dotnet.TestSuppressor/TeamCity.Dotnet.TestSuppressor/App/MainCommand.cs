using TeamCity.Dotnet.TestSuppressor.App.Restore;
using TeamCity.Dotnet.TestSuppressor.App.Suppress;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.TestSuppressor.App;

internal class MainCommand : Command
{
    [Command("suppress")]
    [CommandDescription("Suppresses tests from the specified file in specified target")]
    public SuppressCommand? Suppress { get; set; }

    [Command("restore")]
    [CommandDescription("Restores original assemblies")]
    public RestoreCommand? Restore { get; set; }
}