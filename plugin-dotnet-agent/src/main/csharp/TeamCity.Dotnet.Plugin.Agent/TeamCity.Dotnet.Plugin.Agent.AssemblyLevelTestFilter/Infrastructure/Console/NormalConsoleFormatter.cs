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

using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;
using Microsoft.Extensions.Logging.Console;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;

internal class NormalConsoleFormatter : ConsoleFormatter
{
    private const string MessageFormat = "{0}";

    public NormalConsoleFormatter() : base(nameof(NormalConsoleFormatter)) {}

    public override void Write<TState>(in LogEntry<TState> logEntry, IExternalScopeProvider? scopeProvider, TextWriter textWriter)
    {
        var message = logEntry.Formatter!(logEntry.State, logEntry.Exception);

        message = message.Replace(logEntry.Category, "").Trim('\n'); // remove category

        switch (logEntry.LogLevel)
        {
            case LogLevel.Trace:
                textWriter.WriteLine(MessageFormat, message.Cyan());
                break;
            case LogLevel.Debug:
                textWriter.WriteLine(MessageFormat, message.Magenta());
                break;
            case LogLevel.Information:
                textWriter.WriteLine(MessageFormat, message);
                break;
            case LogLevel.Warning:
                textWriter.WriteLine(MessageFormat, message.Yellow());
                break;
            case LogLevel.Error:
            case LogLevel.Critical:
                textWriter.WriteLine(MessageFormat, message.Red());
                break;
            case LogLevel.None:
            default:
                textWriter.WriteLine(MessageFormat, message.Blue());
                break;
        }
    }
}