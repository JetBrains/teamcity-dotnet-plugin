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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Configuration;

internal static class CommandLineOptions<T>
{
    public static IDictionary<string, string> GenerateMappingsForCommands()
    {
        var mappings = new Dictionary<string, string>();
        GenerateMappingsForType(typeof(T), mappings, typeof(T).Name, typeof(T).Name);
        return mappings;
    }
    
    private static void GenerateMappingsForType(Type type, IDictionary<string, string> mappings, string currentParamPath, string currentPropertyPath)
    {
        // for instance:
        // suppress             --> MainCommand:Suppress:IsActive
        // suppress:-t          --> MainCommand:Suppress:Target, MainCommand:Suppress:IsActive
        // suppress:-h|bool     --> MainCommand:Suppress:Help, MainCommand:Suppress:IsActive
        // -h|bool              --> MainCommand:Help
        // -v                   --> MainCommand:Verbosity
        // etc...
        foreach (var property in type.GetProperties())
        {
            var commandAttribute = property.GetCustomAttribute<CommandAttribute>();
            var optionAttribute = property.GetCustomAttribute<CommandOptionAttribute>();
            
            var propertyPath = string.Join(':', currentPropertyPath, property.Name);
            
            if (commandAttribute != null)
            {
                var paramPath = string.Join(':', currentParamPath, commandAttribute.Command);

                mappings.Add(paramPath, propertyPath);

                // recursively add mappings for all properties of the command
                GenerateMappingsForType(property.PropertyType, mappings, paramPath, propertyPath);
                continue;
            }
            
            if (optionAttribute != null)
            {
                var typeQualificator = GenerateOptionTypeQualificator(property.PropertyType);
                foreach (var option in optionAttribute.Options)
                {
                    var paramPath = string.Join(':', currentParamPath, option) + typeQualificator;
                    mappings.Add(paramPath, propertyPath);
                }
            }
        }
    }

    private static string GenerateOptionTypeQualificator(Type type) =>
        type == typeof(bool) ? "|bool" : string.Empty;
}