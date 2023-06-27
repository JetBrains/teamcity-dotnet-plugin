using System.Reflection;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.Console;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Help;

internal class HelpPrinter : IHelpPrinter
{
    private readonly ILogger<HelpPrinter> _logger;

    public HelpPrinter(ILogger<HelpPrinter> logger)
    {
        _logger = logger;
    }
    
    public void PrintHelp(Command command)
    {
        var commandProperties = command.GetType().GetProperties();
        
        var columnAligner = new ColumnAligner('\t', 4);

        _logger.LogInformation("Available commands and options:");
        foreach (var property in commandProperties)
        {
            // print help for command
            var commandAttribute = property.GetCustomAttribute<CommandAttribute>();
            var commandDescriptionAttribute = property.GetCustomAttribute<CommandDescriptionAttribute>();
            if (commandAttribute != null && commandDescriptionAttribute != null)
            {
                columnAligner.AddRow($"\t{commandAttribute.Command}\t{commandDescriptionAttribute.Description}");
            }
            
            // print help for command option
            var optionAttribute = property.GetCustomAttribute<CommandOptionAttribute>();
            var optionDescriptionAttribute = property.GetCustomAttribute<CommandOptionDescriptionAttribute>();
            if (optionAttribute != null)
            {
                columnAligner.AddRow($"\t{string.Join(" | ", optionAttribute.Options)}\t{optionDescriptionAttribute?.Description ?? "<no description>"}");
            }
        }

        // ReSharper disable once TemplateIsNotCompileTimeConstantProblem
        columnAligner.Flush(message => _logger.LogInformation(message));
    }
}