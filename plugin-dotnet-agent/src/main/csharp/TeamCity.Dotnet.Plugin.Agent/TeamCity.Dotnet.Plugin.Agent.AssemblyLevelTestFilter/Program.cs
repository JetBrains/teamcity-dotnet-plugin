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
using Microsoft.Extensions.Configuration.CommandLine;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Console;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Parsing;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Configuration;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DependencyInjection;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter;

internal static class Program
{
    public static Task Main(string[] args)
    {
        var commandLineParsingResult = new CommandLineParser<MainCommand>().Parse(args);
        
        return Host
            .CreateDefaultBuilder(args)
            .ConfigureAppConfiguration((_, config) =>
            {
                
                // config.AddCommandLine(args, commandLineParsingResult.SwitchMappings);
                config.Add(new MyCommandLineConfigurationSource(commandLineParsingResult.SwitchMappings));
            })
            .ConfigureServices((hostContext, services) =>
            {
                services
                    .Configure<MainCommand>(hostContext.Configuration.GetSection(nameof(MainCommand)))
                    .AddSingleton<MainConsoleFormatter>()
                    .AddLogging(loggingBuilder =>
                    {
                        loggingBuilder.ClearProviders(); // remove default logging providers
                        loggingBuilder.AddFilter("Microsoft", LogLevel.None); // disable Microsoft logging

                        loggingBuilder.AddConsoleFormatter<MainConsoleFormatter, ConsoleFormatterOptions>();
                        loggingBuilder.AddConsole(options =>
                        {
                            options.FormatterName = nameof(MainConsoleFormatter);
                            options.LogToStandardErrorThreshold = LogLevel.Error;
                        });
                        loggingBuilder.SetMinimumLevel(LogLevel.Information);
                    });

                // regular services
                services
                    .AddSingleton(commandLineParsingResult)
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
                    .AddSingletonByInterface<ITestSelectorsFactory>()
                    .AddSingletonByInterface<IBackupMetadataSaver>()
                    .AddSingletonByInterface<IHelpPrinter>()
                    .AddSingletonByInterface<ICommandHandler>()
                    .AddSingletonByInterface<ICmdArgsValidator>()
                    .AddSingletonByInterface<ICommandValidator>();

                // hosted service as an entry point
                services.AddHostedService<CommandRouter<MainCommand>>();
            })
            .StartAsync();
    }
}
