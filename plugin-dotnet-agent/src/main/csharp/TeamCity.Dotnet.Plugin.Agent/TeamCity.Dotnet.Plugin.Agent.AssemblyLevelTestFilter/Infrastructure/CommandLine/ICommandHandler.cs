using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;

internal interface ICommandHandler {}

internal interface ICommandHandler<in TCommand> : ICommandHandler
    where TCommand : Command 
{
    Task ExecuteAsync(TCommand command);
}
