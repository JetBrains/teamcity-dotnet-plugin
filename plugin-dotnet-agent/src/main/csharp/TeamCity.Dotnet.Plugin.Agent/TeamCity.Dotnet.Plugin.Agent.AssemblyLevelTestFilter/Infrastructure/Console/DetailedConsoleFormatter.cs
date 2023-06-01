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

internal class DetailedConsoleFormatter : ConsoleFormatter
{
    public DetailedConsoleFormatter() : base(nameof(DetailedConsoleFormatter)) {}

    public override void Write<TState>(in LogEntry<TState> logEntry, IExternalScopeProvider? scopeProvider,
        TextWriter textWriter)
    {
        var message = logEntry.Formatter!(logEntry.State, logEntry.Exception);
        var logLevel = LogLevelString(logEntry.LogLevel);
        var dateTime = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss.fff");

        message = message.Replace(logEntry.Category, "").Trim('\n'); // remove category
        
        textWriter.WriteLine("[{0}] {1}\t{2}", dateTime, logLevel, message);
    }

    private static string LogLevelString(LogLevel logLevel) => logLevel switch
    {
        LogLevel.Trace => "TRC",
        LogLevel.Debug => "DBG",
        LogLevel.Information => "INF",
        LogLevel.Warning => "WRN",
        LogLevel.Error => "ERR",
        LogLevel.Critical => "CRT",
        LogLevel.None => "UKN",
        _ => throw new ArgumentOutOfRangeException(nameof(logLevel), logLevel, null)
    };
}