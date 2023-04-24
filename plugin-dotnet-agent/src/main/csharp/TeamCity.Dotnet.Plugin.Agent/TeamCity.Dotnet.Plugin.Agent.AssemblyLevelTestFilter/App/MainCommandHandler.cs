using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App;

internal class MainCommandHandler : ICommandHandler<MainCommand>
{
    private readonly IHelpService _helpService;
    private readonly ILogger<MainCommandHandler> _logger;

    public MainCommandHandler(IHelpService helpService, ILogger<MainCommandHandler> logger)
    {
        _helpService = helpService;
        _logger = logger;
    }

    public async Task ExecuteAsync(MainCommand command)
    {
        if (command.Help)
        {
            _helpService.ShowHelpAsync(command);
            return;
        }

        _logger.LogInformation($"No subcommand found in {nameof(MainCommand)}");
    }
}
