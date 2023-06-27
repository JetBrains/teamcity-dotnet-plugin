using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Console;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.Console;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.Logging.Configurators;

internal class NormalLoggerConfigurator : ILoggerConfigurator
{
    public Verbosity Verbosity => Verbosity.Normal;

    public void Configure(ILoggingBuilder builder)
    {
        builder.ClearProviders();
        builder.AddFilter("Microsoft", LogLevel.None);
        
        builder.AddConsoleFormatter<NormalConsoleFormatter, ConsoleFormatterOptions>();
        builder.AddConsole(options =>
        {
            options.FormatterName = nameof(NormalConsoleFormatter);
        });
        builder.SetMinimumLevel(LogLevel.Information);
    }
}