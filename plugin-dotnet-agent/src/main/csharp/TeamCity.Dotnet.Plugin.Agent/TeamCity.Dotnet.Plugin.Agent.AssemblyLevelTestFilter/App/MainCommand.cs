using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Restore;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Suppress;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App;

internal class MainCommand : Command
{
    [Command("suppress")]
    [CommandDescription("Suppresses tests from the specified file in specified target")]
    public SuppressCommand? Suppress { get; set; }

    [Command("restore")]
    [CommandDescription("Restores original assemblies")]
    public RestoreCommand? Restore { get; set; }
}