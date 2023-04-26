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

using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Restore;

internal class RestoreCommandHandler : ICommandHandler<RestoreCommand>
{
    private readonly IHelpPrinter _helpPrinter;

    public RestoreCommandHandler(IHelpPrinter helpPrinter)
    {
        _helpPrinter = helpPrinter;
    }

    public async Task ExecuteAsync(RestoreCommand command)
    {
        if (command.Help)
        {
            _helpPrinter.ShowHelp(command);
            return;
        }

        throw new NotImplementedException();
    }
}