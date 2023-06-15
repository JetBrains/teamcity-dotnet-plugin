using Microsoft.Extensions.Configuration;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Configuration;

internal class CommandLineConfigurationSource : IConfigurationSource
{
    private readonly IDictionary<string, string> _mappings;

    public CommandLineConfigurationSource(IDictionary<string, string> mappings)
    {
        _mappings = mappings;
    }

    public IConfigurationProvider Build(IConfigurationBuilder builder) =>
        new CommandLineConfigurationProvider(_mappings);
}