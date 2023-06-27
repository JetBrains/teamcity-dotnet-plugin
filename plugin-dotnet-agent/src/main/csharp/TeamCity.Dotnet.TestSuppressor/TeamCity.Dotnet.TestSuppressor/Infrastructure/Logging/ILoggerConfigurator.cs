using Microsoft.Extensions.Logging;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.Logging;

internal interface ILoggerConfigurator
{
    Verbosity Verbosity { get; }
    
    void Configure(ILoggingBuilder loggingBuilder);
}