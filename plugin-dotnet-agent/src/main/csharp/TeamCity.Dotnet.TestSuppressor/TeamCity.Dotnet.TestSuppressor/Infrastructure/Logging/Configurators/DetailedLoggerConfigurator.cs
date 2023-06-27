using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Console;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.Console;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.Logging.Configurators;

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