using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;
using Microsoft.Extensions.Logging.Console;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.Console;

internal class NormalConsoleFormatter : ConsoleFormatter
{
    private const string MessageFormat = "{0}";

    public NormalConsoleFormatter() : base(nameof(NormalConsoleFormatter)) {}

    public override void Write<TState>(in LogEntry<TState> logEntry, IExternalScopeProvider? scopeProvider, TextWriter textWriter)
    {
        var message = logEntry.Formatter!(logEntry.State, logEntry.Exception);
        message = message.Replace(logEntry.Category, "").Trim('\n'); // remove category
        textWriter.WriteLine(MessageFormat, message);
    }
}