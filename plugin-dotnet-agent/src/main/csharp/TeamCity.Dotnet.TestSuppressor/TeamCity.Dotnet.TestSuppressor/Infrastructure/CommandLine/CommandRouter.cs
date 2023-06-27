using System.Reflection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Help;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine;

internal class CommandRouter<TCommand>
    where TCommand : Command
{
    private readonly IOptions<TCommand> _options;
    private readonly ICommandValidator _commandValidator;
    private readonly IHelpPrinter _helpPrinter;
    private readonly IEnumerable<ICommandHandler> _commandHandlers;
    private readonly ILogger<CommandRouter<TCommand>> _logger;
    private readonly IHostApplicationLifetime _applicationLifetime;

    public CommandRouter(
        IOptions<TCommand> options,
        ICommandValidator commandValidator,
        IHelpPrinter helpPrinter,
        IEnumerable<ICommandHandler> commandHandlers,
        ILogger<CommandRouter<TCommand>> logger,
        IHostApplicationLifetime applicationLifetime)
    {
        _options = options;
        _commandValidator = commandValidator;
        _helpPrinter = helpPrinter;
        _commandHandlers = commandHandlers;
        _logger = logger;
        _applicationLifetime = applicationLifetime;
    }
    
    public async Task Route()
    {
        var rootCommand = _options.Value;
        
        // if help is requested
        var subCommand = GetSelectedSubcommand(rootCommand);
        if (rootCommand.Help || subCommand is { IsActive: true, Help: true })
        {
            _helpPrinter.PrintHelp(subCommand ?? rootCommand);
            _applicationLifetime.StopApplication();
            return;
        }
        
        // then validate the command
        var validationResult = _commandValidator.Validate(rootCommand);
        if (!validationResult.IsValid)
        {
            _logger.LogError("Command validation failed");
            _logger.LogError("{ValidationResultErrorMessage}", validationResult.ErrorMessage);
            _helpPrinter.PrintHelp(rootCommand);
            _applicationLifetime.StopApplication();
            return;
        }

        // then execute the command if subcommand is specified
        if (subCommand is not { IsActive: true })
        {
            _logger.LogWarning("No command or root level options specified");
            _helpPrinter.PrintHelp(rootCommand);
            _applicationLifetime.StopApplication();
            return;
        }
        
        var handler = GetCommandHandler(subCommand);
        
        await ExecuteHandler(handler, subCommand);
        
        _applicationLifetime.StopApplication();
    }

    private ICommandHandler GetCommandHandler(Command selectedCommand)
    {
        var handler = _commandHandlers
            .First(handler => handler.GetType().GetInterfaces().Any(i => i.GenericTypeArguments.Contains(selectedCommand.GetType())));
        if (handler == null)
        {
            throw new InvalidOperationException($"No handler found for {selectedCommand.GetType()}");
        }

        return handler;
    }

    private static Command? GetSelectedSubcommand(Command command) => command
        .GetType()
        .GetProperties()
        .Where(prop => prop.GetCustomAttribute<CommandAttribute>() != null && prop.GetValue(command) != null)
        .Select(prop => (Command)prop.GetValue(command)!)
        .FirstOrDefault();

    private static Task ExecuteHandler(ICommandHandler handler, Command subCommand) =>
        (Task) handler.GetType().GetMethod("ExecuteAsync")!.Invoke(handler, new object[] {subCommand})!;
}
