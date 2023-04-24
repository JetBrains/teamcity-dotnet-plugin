using System.Reflection;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;

internal class HelpService : IHelpService
{
    private readonly ILogger<HelpService> _logger;

    public HelpService(ILogger<HelpService> logger)
    {
        _logger = logger;
    }
    
    public void ShowHelpAsync(Command command)
    {
        var commandProperties = command.GetType().GetProperties()
            .Where(prop => prop.GetCustomAttribute<CommandLineOptionAttribute>() != null)
            .ToList();

        _logger.LogInformation($"Options for {command.GetType().Name}:");
        foreach (var property in commandProperties)
        {
            var optionAttribute = property.GetCustomAttribute<CommandLineOptionAttribute>();
            var descriptionAttribute = property.GetCustomAttribute<CommandDescriptionAttribute>();

            _logger.LogInformation($"{string.Join(", ", optionAttribute.Options)}: {descriptionAttribute.Description}");
        }
    }
}