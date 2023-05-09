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

using System.Reflection;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;

internal class HelpPrinter : IHelpPrinter
{
    private readonly ILogger<HelpPrinter> _logger;

    public HelpPrinter(ILogger<HelpPrinter> logger)
    {
        _logger = logger;
    }
    
    public void PrintHelp(Command command)
    {
        var commandProperties = command.GetType().GetProperties();
        
        var columnAligner = new ColumnAligner('\t', 4);

        _logger.LogInformation("Available commands and options:");
        foreach (var property in commandProperties)
        {
            // print help for command
            var commandAttribute = property.GetCustomAttribute<CommandAttribute>();
            var commandDescriptionAttribute = property.GetCustomAttribute<CommandDescriptionAttribute>();
            if (commandAttribute != null && commandDescriptionAttribute != null)
            {
                columnAligner.AddRow($"\t{commandAttribute.Command}\t{commandDescriptionAttribute.Description}");
            }
            
            // print help for command option
            var optionAttribute = property.GetCustomAttribute<CommandOptionAttribute>();
            var optionDescriptionAttribute = property.GetCustomAttribute<CommandOptionDescriptionAttribute>();
            if (optionAttribute != null)
            {
                columnAligner.AddRow($"\t{string.Join(" | ", optionAttribute.Options)}\t{optionDescriptionAttribute?.Description ?? "<no description>"}");
            }
        }

        // ReSharper disable once TemplateIsNotCompileTimeConstantProblem
        columnAligner.Flush(message => _logger.LogInformation(message));
    }
}