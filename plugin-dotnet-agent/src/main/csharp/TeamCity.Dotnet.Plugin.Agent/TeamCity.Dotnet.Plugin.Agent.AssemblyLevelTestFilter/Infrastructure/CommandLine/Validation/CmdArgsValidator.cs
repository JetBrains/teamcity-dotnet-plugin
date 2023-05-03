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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

internal class CmdArgsValidator : ICmdArgsValidator
{
    public ValidationResult Validate(Type commandType)
    {
        var args = Environment.GetCommandLineArgs()[1..].ToList();
        var argsInfo = GetArgsFromCommandType(commandType);
        var unknownArgs = new List<string>();

        for (var i = 0; i < args.Count; i++)
        {
            if (!argsInfo.ContainsKey(args[i]))
            {
                unknownArgs.Add(args[i]);
            }
            else if (argsInfo[args[i]])
            {
                i++; // skip value
            }
        }

        return unknownArgs.Count != 0
            ? ValidationResult.Invalid($"Unknown arguments: {string.Join(", ", unknownArgs)}")
            : ValidationResult.Valid;
    }

    private static IReadOnlyDictionary<string, bool> GetArgsFromCommandType(Type commandType)
    {
        var result = new Dictionary<string, bool>();
        foreach (var property in commandType.GetProperties())
        {
            var command = property.GetCustomAttribute<CommandAttribute>()?.Command;
            if (command != null)
            {
                result.Add(command, false);

                var argsFromNestedCommandType = GetArgsFromCommandType(property.PropertyType);
                foreach (var pair in argsFromNestedCommandType)
                {
                    result[pair.Key] = pair.Value;
                }
            }

            var commandOption = property.GetCustomAttribute<CommandOptionAttribute>();
            if (commandOption != null)
            {
                foreach (var option in commandOption.Options)
                {
                    result[option] = commandOption.RequiresValue;
                }
            }
        }

        return result;
    }

}