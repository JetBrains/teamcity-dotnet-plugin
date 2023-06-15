using Microsoft.Extensions.Logging;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Logging.Configurators;

internal class DiagnosticLoggerConfigurator : ILoggerConfigurator
{
    public Verbosity Verbosity => Verbosity.Diagnostic;
    
    public void Configure(ILoggingBuilder loggingBuilder) => loggingBuilder
        .SetMinimumLevel(LogLevel.Trace)
        .AddFilter("Microsoft", LogLevel.Trace)
        .AddSimpleConsole(options => options.TimestampFormat = "[yyyy-MM-dd HH:mm:ss.fff] ");
}