using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure.Console;

public class NormalConsoleFormatterTests
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
        var formatter = new NormalConsoleFormatter();
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
