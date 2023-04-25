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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;

internal class CommandRouter<TCommand> : IHostedService
    where TCommand : Command
{
    private readonly TCommand _command;
    private readonly IEnumerable<ICommandHandler> _commandHandlers;

    public CommandRouter(TCommand command, IEnumerable<ICommandHandler> commandHandlers)
    {
        _command = command;
        _commandHandlers = commandHandlers;
    }
    
    public Task StartAsync(CancellationToken cancellationToken)
    {
        var subCommand = GetSelectedSubcommand(_command);
        var handler = GetCommandHandler(subCommand);
        return ExecuteHandler(cancellationToken, handler, subCommand);
    }

    public Task StopAsync(CancellationToken cancellationToken)
    {
        return Task.CompletedTask;
    }

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

    private static Command GetSelectedSubcommand(Command command)
    {
        var selectedCommand = command.GetType().GetProperties()
            .Where(prop => prop.GetCustomAttribute<CommandAttribute>() != null && prop.GetValue(command) != null)
            .Select(prop => (Command)prop.GetValue(command)!)
            .FirstOrDefault();
        
        if (selectedCommand == null)
        {
            throw new InvalidOperationException($"No subcommand found in {nameof(TCommand)}");
        }

        return selectedCommand;
    }

    private static Task ExecuteHandler(CancellationToken cancellationToken, ICommandHandler handler, Command subCommand) =>
        (Task) handler.GetType().GetMethod("ExecuteAsync")!
            .Invoke(handler, new object[] {subCommand, cancellationToken})!;
}
