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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Configuration;

internal class CommandLineConfigurationSource : IConfigurationSource
{
    private readonly string[] _args;
    private readonly IDictionary<string, string> _mappings;

    public CommandLineConfigurationSource(string[] args, IDictionary<string, string> mappings)
    {
        _args = args;
        _mappings = mappings;
    }

    public IConfigurationProvider Build(IConfigurationBuilder builder)
    {
        return new CommandLineConfigurationProvider(_args, _mappings);
    }
}