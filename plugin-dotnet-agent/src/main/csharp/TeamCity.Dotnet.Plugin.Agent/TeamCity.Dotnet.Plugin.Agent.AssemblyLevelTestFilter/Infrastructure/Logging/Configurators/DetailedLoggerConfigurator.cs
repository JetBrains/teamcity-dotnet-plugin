using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Console;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Logging.Configurators;

internal class DetailedLoggerConfigurator : ILoggerConfigurator
{
    public Verbosity Verbosity => Verbosity.Detailed;

    public void Configure(ILoggingBuilder loggingBuilder)
    {
        loggingBuilder.AddFilter("Microsoft", LogLevel.Information);
        
        loggingBuilder.AddConsoleFormatter<DetailedConsoleFormatter, ConsoleFormatterOptions>();
        loggingBuilder.AddConsole(options =>
        {
            options.FormatterName = nameof(DetailedConsoleFormatter);
        });
        loggingBuilder.SetMinimumLevel(LogLevel.Debug);
    }
}