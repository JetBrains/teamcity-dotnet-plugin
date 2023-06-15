using Microsoft.Extensions.Logging;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Logging.Configurators;

internal class QuietLoggerConfigurator : ILoggerConfigurator
{
    public Verbosity Verbosity => Verbosity.Quiet;

    public void Configure(ILoggingBuilder builder) => builder
        .ClearProviders()
        .SetMinimumLevel(LogLevel.None)
        .AddFilter("Microsoft", LogLevel.None);
}