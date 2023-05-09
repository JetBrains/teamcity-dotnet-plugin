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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure.Console;

public class MainConsoleFormatterTests
{
    [Theory]
    [InlineData(LogLevel.Trace, "\x1b[36mHello, World!\x1b[39m\n")]
    [InlineData(LogLevel.Debug, "\x1b[35mHello, World!\x1b[39m\n")]
    [InlineData(LogLevel.Information, "Hello, World!\n")]
    [InlineData(LogLevel.Warning, "\x1b[33mHello, World!\x1b[39m\n")]
    [InlineData(LogLevel.Error, "\x1b[31mHello, World!\x1b[39m\n")]
    [InlineData(LogLevel.Critical, "\x1b[31mHello, World!\x1b[39m\n")]
    public void Write_WritesFormattedMessage(LogLevel logLevel, string expectedOutput)
    {
        // arrange
        var formatter = new MainConsoleFormatter();
        var logEntry = new LogEntry<string>(
            logLevel: logLevel,
            category: "TestCategory",
            eventId: new EventId(),
            state: "Hello, World!",
            exception: null,
            formatter: (s, _) => s);
        var scopeProvider = (IExternalScopeProvider?)null;
        var stringWriter = new StringWriter();

        // act
        formatter.Write(logEntry, scopeProvider, stringWriter);
        var actualOutput = stringWriter.ToString();

        // assert
        Assert.Equal(expectedOutput, actualOutput);
    }
}
