using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;
using Microsoft.Extensions.Logging.Console;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.Console;

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