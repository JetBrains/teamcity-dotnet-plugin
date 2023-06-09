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

using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

internal class DotnetAssembly : IDotnetAssembly
{
    private readonly AssemblyDefinition _assemblyDefinition;

    public DotnetAssembly(AssemblyDefinition assemblyDefinition)
    {
        _assemblyDefinition = assemblyDefinition;
    }

    public bool HasSymbols => _assemblyDefinition.MainModule.HasSymbols;
    
    public void Write(FileStream destinationFileStream, bool withSymbols)
    {
        _assemblyDefinition.Write(destinationFileStream, new WriterParameters { WriteSymbols = withSymbols });
    }

    public IEnumerable<IDotnetType> Types => _assemblyDefinition.Modules
        .SelectMany(module => module.Types)
        .Select(t => new DotnetType(t));

    public void Dispose()
    {
        _assemblyDefinition.Dispose();
    }
}