using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter;

internal static class Program
{
    public static void Main(string[] args) => Host
        .CreateDefaultBuilder(args)
        .ConfigureAppConfiguration((_, config) =>
        {
            config.AddCommandLine(args, CommandLineOptions<Settings>.Mappings);
        })
        .ConfigureServices((hostContext, services) =>
        {
            services
                .Configure<Settings>(hostContext.Configuration.GetSection("Settings"))
                .AddLogging(loggingBuilder =>
                {
                    loggingBuilder.AddConsole();
                    loggingBuilder.SetMinimumLevel(LogLevel.Debug);
                })
                .AddHostedService<AssemblyPatcher>()
                .AddSingletonByInterface<ITestQueryParser>()
                .AddSingletonByInterface<ITestEngine>()
                .AddSingletonByInterface<ITestEngineRecognizer>()
                .AddSingletonByInterface<ITestsSuppressionResolver>()
                .AddSingletonByInterface<ITestSuppressingStrategy>()
                .AddSingletonByInterface<ITestsSuppressor>();
            
            services.DisplayRegisteredServices();

        })
        .Build()
        .Run();
}
