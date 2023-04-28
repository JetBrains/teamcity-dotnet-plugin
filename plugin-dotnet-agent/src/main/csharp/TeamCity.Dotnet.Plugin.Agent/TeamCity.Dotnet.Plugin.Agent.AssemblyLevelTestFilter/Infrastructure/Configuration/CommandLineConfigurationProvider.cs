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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Configuration;

public class CommandLineConfigurationProvider : ConfigurationProvider
{
    private readonly IDictionary<string, string> _mappings;
    private readonly string[] _args;

    public CommandLineConfigurationProvider(string[] args, IDictionary<string, string> mappings)
    {
        _args = args;
        _mappings = mappings;
    }

    public override void Load()
    {
        Data = CreateArgsDictionary(_args, _mappings)!;
    }
    
    private static IDictionary<string, string> CreateArgsDictionary(string[] args, IDictionary<string, string> mappings)
    {
        var result = new Dictionary<string, string>();
        
        // todo this code is a mess, need to be refactored

        var argsPath = new List<string>();

        for (var i = 0; i < args.Length; i++)
        {
            var argument = args[i];
            string key;
            string value;
            
            argsPath.Add(argument);
            
            var (mappingKey, typeQualificator) = FindMappingKey(mappings, argsPath);
            
            // if starts with command, not an option
            if (i == 0 && !argument.StartsWith("-"))
            {
                // every command has IsActive flag to indicate that command is present and activated
                // it's needed to instantiate a command object
                key = $"{mappings[mappingKey]}:{nameof(Command.IsActive)}";
                value = "true";
            }
            else
            {
                // if no mapping found – skip
                if (!mappings.TryGetValue(mappingKey, out var maybeKey))
                {
                    continue;
                }

                key = maybeKey;
                
                // if next arg is not an option
                if (i + 1 < args.Length && !args[i + 1].StartsWith("-"))
                {
                    value = typeQualificator == "bool" ? "true" : args[i + 1];
                    i++;
                }
                else // if next arg is an option
                {
                    // if option is bool type – means it's a flag and should be interpreted as "true"
                    // if option without type – means it's unfinished flag and should be interpreted as an empty string
                    value = typeQualificator == "bool" ? "true" : string.Empty;
                }
            }
            
            if (!string.IsNullOrEmpty(key))
            {
                result[key] = value;
            }
        }

        return result;
    }

    private static (string key, string typeQualificator) FindMappingKey(IDictionary<string, string> mappings, IReadOnlyList<string> argsPath)
    {
        var key = string.Join(':', argsPath);
        
        foreach (var mapping in mappings)
        {
            var mappingKeyParts = mapping.Key.Split('|');
            
            var mappingKey = string.Join(':', mappingKeyParts[0].Split(':')[1..]);
            if (mappingKey != key)
            {
                continue;
            }
            
            var typeQualificator = mappingKeyParts.Length == 1 ? string.Empty : mappingKeyParts[1];
            return (mapping.Key, typeQualificator);
        }

        return (string.Empty, string.Empty);
    }
}