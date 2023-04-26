/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DependencyInjection;

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
                });

            // regular services
            services
                .AddSingletonByInterface<ITestSelectorParser>()
                .AddSingletonByInterface<ITestEngine>()
                .AddSingletonByImplementationType<ITestEngine>()
                .AddSingletonByInterface<ITestEngineRecognizer>()
                .AddSingletonByInterface<ITestClassDetector>()
                .AddSingletonByInterface<ITestSuppressingStrategy>()
                .AddSingletonByInterface<ITestsSuppressor>()
                .AddSingletonByInterface<ITargetResolvingStrategy>()
                .AddSingletonByInterface<ITargetResolver>()
                .AddSingletonByInterface<IAssemblyPatcher>()
                .AddSingletonByInterface<IBackupMetadataSaver>()
                .AddSingletonByInterface<IHelpPrinter>()
                .AddSingletonByInterface<ICommandHandler>()
                .AddSingleton<MainCommand>();

            // hosted services
            services
                .AddHostedService<CommandValidator<MainCommand>>()  // if validator fails, the app will be stopped
                .AddHostedService<CommandRouter<MainCommand>>();    // entrypoint for the app, finds and executes appropriate command handler
            
            services.DisplayRegisteredServices();
        })
        .Build()
        .Run();
}
