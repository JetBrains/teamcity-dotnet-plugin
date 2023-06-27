using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine;

internal interface ICommandHandler {}

internal interface ICommandHandler<in TCommand> : ICommandHandler
    where TCommand : Command 
{
    Task ExecuteAsync(TCommand command);
}
