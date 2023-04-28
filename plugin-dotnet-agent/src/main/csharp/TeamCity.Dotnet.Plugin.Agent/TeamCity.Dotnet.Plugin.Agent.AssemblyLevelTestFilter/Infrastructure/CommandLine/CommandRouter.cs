/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System.Reflection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;

internal class CommandRouter<TCommand> : IHostedService
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
    
    public async Task StartAsync(CancellationToken cancellationToken)
    {
        var rootCommand = _options.Value;
        
        // at first we validate the command
        var validationResult = _commandValidator.Validate(rootCommand);
        if (!validationResult.IsValid)
        {
            _logger.LogError("Command validation failed");
            _logger.LogError("{ValidationResultErrorMessage}", validationResult.ErrorMessage);
            _helpPrinter.PrintHelp(rootCommand);
            _applicationLifetime.StopApplication();
            return;
        }
        
        // then we check if help is requested
        var subCommand = GetSelectedSubcommand(rootCommand);
        if (rootCommand.Help || subCommand is { IsActive: true, Help: true })
        {
            _helpPrinter.PrintHelp(subCommand ?? rootCommand);
            _applicationLifetime.StopApplication();
            return;
        }

        // then we execute the command if subcommand is specified
        if (subCommand == null)
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

    public Task StopAsync(CancellationToken cancellationToken) => Task.CompletedTask;

    private ICommandHandler GetCommandHandler(Command selectedCommand)
    {
        var handler = _commandHandlers
            .First(handler => handler.GetType().GetInterfaces().First().GetGenericArguments()[0] == selectedCommand.GetType());
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
