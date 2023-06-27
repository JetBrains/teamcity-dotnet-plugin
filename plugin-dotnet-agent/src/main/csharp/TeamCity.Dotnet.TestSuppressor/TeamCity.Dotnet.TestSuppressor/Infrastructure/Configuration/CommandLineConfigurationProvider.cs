using Microsoft.Extensions.Configuration;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.Configuration;

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

