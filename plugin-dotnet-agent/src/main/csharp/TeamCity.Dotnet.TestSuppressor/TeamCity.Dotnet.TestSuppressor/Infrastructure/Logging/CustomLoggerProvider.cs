using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.Logging;

internal class CustomLoggerProvider<TCommand> : ILoggerProvider
    where TCommand : Command
{
    private readonly IOptions<TCommand> _options;
    private readonly IEnumerable<ILoggerConfigurator> _loggerConfigurators;

    public CustomLoggerProvider(
        IOptions<TCommand> options, 
        IEnumerable<ILoggerConfigurator> loggerConfigurators)
    {
        _options = options;
        _loggerConfigurators = loggerConfigurators;
    }

    public ILogger CreateLogger(string categoryName)
    {
        var command = _options.Value;
        var loggerConfigurator = ChooseConfigurator(command.Verbosity);
        var loggerFactory = LoggerFactory.Create(builder => loggerConfigurator.Configure(builder));
        return loggerFactory.CreateLogger(categoryName);
    }

    private ILoggerConfigurator ChooseConfigurator(Verbosity verbosity = Verbosity.Normal) =>
        _loggerConfigurators.FirstOrDefault(x => x.Verbosity == verbosity)
        ?? _loggerConfigurators.First(x => x.Verbosity == Verbosity.Normal);

    public void Dispose() {}
}