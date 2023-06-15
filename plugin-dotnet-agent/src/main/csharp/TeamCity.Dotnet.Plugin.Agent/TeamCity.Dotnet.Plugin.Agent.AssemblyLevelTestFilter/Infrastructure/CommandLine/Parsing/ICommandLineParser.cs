using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Parsing;

internal interface ICommandLineParser<TCommand>
    where TCommand : Command
{
    IConfigurationParsingResult Parse(IEnumerable<string> args);
}