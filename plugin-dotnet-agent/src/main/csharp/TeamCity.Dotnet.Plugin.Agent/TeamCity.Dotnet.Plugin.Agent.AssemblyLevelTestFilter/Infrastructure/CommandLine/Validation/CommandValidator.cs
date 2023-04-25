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
