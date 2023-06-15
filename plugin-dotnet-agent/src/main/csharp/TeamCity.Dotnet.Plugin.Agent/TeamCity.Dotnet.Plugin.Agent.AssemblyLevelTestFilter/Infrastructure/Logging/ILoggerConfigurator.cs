using Microsoft.Extensions.Logging;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Logging;

internal interface ILoggerConfigurator
{
    Verbosity Verbosity { get; }
    
    void Configure(ILoggingBuilder loggingBuilder);
}