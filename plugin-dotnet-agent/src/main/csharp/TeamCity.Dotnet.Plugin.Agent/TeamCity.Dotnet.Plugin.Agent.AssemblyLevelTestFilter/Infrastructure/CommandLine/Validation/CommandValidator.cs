using System.Reflection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

public class CommandValidator<T> : IHostedService where T : class
{
    private readonly T _settings;
    private readonly IHostApplicationLifetime _applicationLifetime;
    private readonly ILogger<CommandValidator<T>> _logger;

    public CommandValidator(
        IOptions<T> settings,
        IHostApplicationLifetime applicationLifetime,
        ILogger<CommandValidator<T>> logger)
    {
        _settings = settings.Value;
        _applicationLifetime = applicationLifetime;
        _logger = logger;
    }

    public Task StartAsync(CancellationToken cancellationToken)
    {
        var validationResult = Validate(_settings);
        if (validationResult.IsValid)
        {
            return Task.CompletedTask;
        }

        _logger.LogError($"Command validation failed:\n{validationResult.ErrorMessage}");
        _applicationLifetime.StopApplication();

        return Task.CompletedTask;
    }

    public Task StopAsync(CancellationToken cancellationToken) => Task.CompletedTask;

    private static ValidationResult Validate(T settings)
    {
        var validationErrors = new List<string>();
        var properties = typeof(T).GetProperties();

        foreach (var property in properties)
        {
            var value = property.GetValue(settings);
            var validationAttributes = property.GetCustomAttributes<ValidationAttribute>();
            var requiredAttribute = property.GetCustomAttribute<RequiredAttribute>();

            if (requiredAttribute != null && value == null)
            {
                validationErrors.Add(requiredAttribute.ErrorMessage);
            }

            foreach (var attribute in validationAttributes)
            {
                var validationResult = attribute.IsValid(value);
                if (!validationResult.IsValid)
                {
                    validationErrors.Add(validationResult.ErrorMessage);
                }
            }
        }

        return validationErrors.Count != 0
            ? ValidationResult.Invalid(string.Join(Environment.NewLine, validationErrors))
            : ValidationResult.Valid;
    }
}
