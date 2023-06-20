using Microsoft.Extensions.Configuration;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Parsing;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Configuration;

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