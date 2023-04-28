// /*
//  * Copyright 2000-2023 JetBrains s.r.o.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  * http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */
//
// using System.Reflection;
//
// namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
//
// internal static class CommandLineOptions<T>
// {
//     public static IDictionary<string, IDictionary<string, string>> GetMappings()
//     {
//         var mappings = new Dictionary<string, IDictionary<string, string>>();
//
//         AddMappingsForType(typeof(T), mappings, null);
//
//         return mappings;
//     }
//
//     private static void AddMappingsForType(Type type, IDictionary<string, IDictionary<string, string>> mappings, string? currentCommand)
//     {
//         var className = type.Name;
//         var properties = type.GetProperties();
//
//         foreach (var property in properties)
//         {
//             var commandAttribute = property.GetCustomAttribute<CommandAttribute>();
//             var optionAttribute = property.GetCustomAttribute<CommandLineOptionAttribute>();
//
//             if (commandAttribute == null && optionAttribute == null)
//             {
//                 continue;
//             }
//
//             var configPath = $"{className}:{property.Name}";
//
//             if (commandAttribute != null)
//             {
//                 currentCommand = commandAttribute.Command;
//
//                 if (!mappings.ContainsKey(currentCommand))
//                 {
//                     mappings[currentCommand] = new Dictionary<string, string>();
//                 }
//
//                 // recursively add mappings for all properties of the command
//                 var commandType = property.PropertyType;
//                 if (typeof(Command).IsAssignableFrom(commandType))
//                 {
//                     AddMappingsForType(commandType, mappings, currentCommand);
//                 }
//             }
//
//             if (optionAttribute != null && currentCommand != null)
//             {
//                 var commandOptions = mappings[currentCommand];
//
//                 foreach (var option in optionAttribute.Options)
//                 {
//                     commandOptions[option] = configPath;
//                 }
//             }
//         }
//     }
// }
//
