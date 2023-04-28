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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

internal class CommandValidator : ICommandValidator
{
    private readonly ICmdArgsValidator _cmdArgsValidator;

    public CommandValidator(ICmdArgsValidator cmdArgsValidator)
    {
        _cmdArgsValidator = cmdArgsValidator;
    }

    public ValidationResult Validate(Command command)
    {
        var argsValidationResult = _cmdArgsValidator.Validate(command.GetType());
        return argsValidationResult.IsValid
            ? ValidateProperties(command)
            : argsValidationResult;
    }

    private ValidationResult ValidateProperties(Command command)
    {
        var validationErrors = new List<string>();

        // check command properties
        foreach (var property in command.GetType().GetProperties())
        {
            var value = property.GetValue(command);
            
            if (value is Command { IsActive: true } nestedCommand)
            {
                var nestedValidationResult = ValidateProperties(nestedCommand);
                if (!nestedValidationResult.IsValid)
                {
                    var commandAttribute = property.GetCustomAttribute<CommandAttribute>()!;
                    validationErrors.Add($"Errors in command `{commandAttribute.Command}`:");
                    validationErrors.Add(nestedValidationResult.ErrorMessage);
                    continue;
                }
            }
            
            var validationAttributes = property.GetCustomAttributes<ValidationAttribute>();
            var requiredAttribute = property.GetCustomAttribute<RequiredAttribute>();

            // check required attribute
            if (requiredAttribute != null && (value == null || (string)value == string.Empty))
            {
                validationErrors.Add(FormatValidationError(requiredAttribute.ErrorMessage));
                continue;
            }

            // check other validation attributes
            foreach (var attribute in validationAttributes)
            {
                var validationResult = attribute.IsValid(value);
                if (!validationResult.IsValid)
                {
                    validationErrors.Add(FormatValidationError(validationResult.ErrorMessage));
                }
            }
        }

        return validationErrors.Count != 0
            ? ValidationResult.Invalid(string.Join(Environment.NewLine, validationErrors))
            : ValidationResult.Valid;
    }

    private static string FormatValidationError(string message) => "  - " + message;
}
