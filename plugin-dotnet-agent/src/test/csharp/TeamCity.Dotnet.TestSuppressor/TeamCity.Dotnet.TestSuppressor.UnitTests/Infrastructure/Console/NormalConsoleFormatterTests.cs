using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.Console;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure.Console;

public class NormalConsoleFormatterTests
{
    [Theory]
    [InlineData(LogLevel.Trace)]
    [InlineData(LogLevel.Debug)]
    [InlineData(LogLevel.Information)]
    [InlineData(LogLevel.Warning)]
    [InlineData(LogLevel.Error)]
    [InlineData(LogLevel.Critical)]
    public void Write_WritesFormattedMessage(LogLevel logLevel)
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
        Assert.Equal("Hello, World!\n", actualOutput);
    }
}
