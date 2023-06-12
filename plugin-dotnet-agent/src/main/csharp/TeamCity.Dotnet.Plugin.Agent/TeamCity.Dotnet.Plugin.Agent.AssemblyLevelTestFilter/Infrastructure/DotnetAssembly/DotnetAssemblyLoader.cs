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

using Microsoft.Extensions.Logging;
using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

internal class DotnetAssemblyLoader : IDotnetAssemblyLoader
{
    private readonly ILogger<DotnetAssemblyLoader> _logger;

    public DotnetAssemblyLoader(ILogger<DotnetAssemblyLoader> logger)
    {
        _logger = logger;
    }
    
    public IDotnetAssembly? LoadAssembly(string assemblyPath, bool withSymbols)
    {
        try
        {
            var assemblyDefinition = AssemblyDefinition.ReadAssembly(assemblyPath, new ReaderParameters
            {
                ReadSymbols = withSymbols, // read debug symbols if available
            });

            return new DotnetAssembly(assemblyDefinition);
        }
        catch (BadImageFormatException exception)
        {
            _logger.LogWarning(exception, "Can't read assembly definition: {Target}", assemblyPath);
            return null;
        }
    }
}