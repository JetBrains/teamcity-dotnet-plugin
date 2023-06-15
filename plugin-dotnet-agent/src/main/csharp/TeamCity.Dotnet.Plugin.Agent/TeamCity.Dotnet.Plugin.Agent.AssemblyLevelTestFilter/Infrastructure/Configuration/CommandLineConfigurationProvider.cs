using Microsoft.Extensions.Configuration;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Configuration;

internal class CommandLineConfigurationProvider : ConfigurationProvider
{
    private readonly IDictionary<string, string> _mappings;

    public CommandLineConfigurationProvider(IDictionary<string, string> mappings)
    {
        _mappings = mappings;
    }

    public override void Load()
    {
        Data = _mappings!;
    }
}

