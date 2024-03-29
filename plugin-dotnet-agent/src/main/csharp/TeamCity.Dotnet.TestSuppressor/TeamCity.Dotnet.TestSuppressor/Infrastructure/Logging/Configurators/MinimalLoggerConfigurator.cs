using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Console;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.Console;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.Logging.Configurators;

internal class MinimalLoggerConfigurator : ILoggerConfigurator
{
    public Verbosity Verbosity => Verbosity.Minimal;

    public void Configure(ILoggingBuilder builder)
    {
        builder.ClearProviders();
        builder.AddFilter("Microsoft", LogLevel.None);
        
        builder.AddConsoleFormatter<NormalConsoleFormatter, ConsoleFormatterOptions>();
        builder.AddConsole(options =>
        {
            options.FormatterName = nameof(NormalConsoleFormatter);
            options.LogToStandardErrorThreshold = LogLevel.Warning;
        });
        builder.SetMinimumLevel(LogLevel.Warning);
    }
}