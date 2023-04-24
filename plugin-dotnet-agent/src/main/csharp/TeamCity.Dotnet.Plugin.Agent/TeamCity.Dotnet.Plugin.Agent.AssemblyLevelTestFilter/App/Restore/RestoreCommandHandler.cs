using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Restore;

internal class RestoreCommandHandler : ICommandHandler<RestoreCommand>
{
    private readonly IHelpService _helpService;

    public RestoreCommandHandler(IHelpService helpService)
    {
        _helpService = helpService;
    }

    public async Task ExecuteAsync(RestoreCommand command)
    {
        if (command.Help)
        {
            _helpService.ShowHelpAsync(command);
            return;
        }

        throw new NotImplementedException();
    }
}