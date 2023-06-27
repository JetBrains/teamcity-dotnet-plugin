using Microsoft.Extensions.Configuration;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Parsing;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.Configuration;

internal class CommandLineConfigurationSource<TCommand> : IConfigurationSource
    where TCommand : Command
{
    public CommandLineConfigurationSource(IEnumerable<string> commandLineArguments)
    {
        var parser = new CommandLineParser<TCommand>();
        ConfigurationParsingResult = parser.Parse(commandLineArguments);
    }

    public IConfigurationParsingResult ConfigurationParsingResult { get; }

    public IConfigurationProvider Build(IConfigurationBuilder builder) =>
        new CommandLineConfigurationProvider(ConfigurationParsingResult.Mappings);
}