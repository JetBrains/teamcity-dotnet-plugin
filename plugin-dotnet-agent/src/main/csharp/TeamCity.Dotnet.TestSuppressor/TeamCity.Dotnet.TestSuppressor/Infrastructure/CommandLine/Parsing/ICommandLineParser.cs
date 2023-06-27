using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Parsing;

internal interface ICommandLineParser<TCommand>
    where TCommand : Command
{
    IConfigurationParsingResult Parse(IEnumerable<string> args);
}