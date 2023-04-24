using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter;

internal static class Program
{
    public static void Main(string[] args) => Host
        .CreateDefaultBuilder(args)
        .ConfigureAppConfiguration((_, config) =>
        {
            config.AddCommandLine(args, CommandLineOptions<MainCommand>.Mappings);
        })
        .ConfigureServices((hostContext, services) =>
        {
            services
                .Configure<MainCommand>(hostContext.Configuration.GetSection("Settings"))
                .AddLogging(loggingBuilder =>
                {
                    loggingBuilder.AddConsole();
                    loggingBuilder.SetMinimumLevel(LogLevel.Debug);
                })
                .AddSingletonByInterface<ITestSelectorParser>()
                .AddSingletonByInterface<ITestEngine>()
                .AddSingletonByImplementationType<ITestEngine>()
                .AddSingletonByInterface<ITestEngineRecognizer>()
                .AddSingletonByInterface<ITestsSuppressionResolver>()
                .AddSingletonByInterface<ITestSuppressingStrategy>()
                .AddSingletonByInterface<ITestsSuppressor>()
                .AddSingletonByInterface<ICommandHandler>()
                .AddSingletonByInterface<IAssemblyPatcher>()
                .AddSingletonByInterface<ITargetResolver>();
            
            // hosted services
            services
                .AddHostedService<CommandValidator<MainCommand>>()  // if validator fails, the app will be stopped
                .AddHostedService<CommandRouter<MainCommand>>();    // entrypoint for the app, finds and executes appropriate command handler
            
            services.DisplayRegisteredServices();
        })
        .Build()
        .Run();
}
