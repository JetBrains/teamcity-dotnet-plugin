using System.Reflection;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Parsing;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

internal class CommandValidator : ICommandValidator
{
    private readonly IConfigurationParsingResult _commandLineParsingResult;

    public CommandValidator(IConfigurationParsingResult commandLineParsingResult)
    {
        _commandLineParsingResult = commandLineParsingResult;
    }

    public ValidationResult Validate(Command command)
    {
        return _commandLineParsingResult.UnknownParameters.Count != 0
            ? ValidationResult.Invalid($"Unknown arguments: {string.Join(", ", _commandLineParsingResult.UnknownParameters)}")
            : ValidateProperties(command);
    }

    private static ValidationResult ValidateProperties(Command command)
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
            if (requiredAttribute != null && string.IsNullOrEmpty(value as string))
            {
                validationErrors.Add(FormatValidationError(requiredAttribute.ErrorMessage));
                continue;
            }

            // check other validation attributes
            foreach (var attribute in validationAttributes)
            {
                var validationResult = attribute.IsValid(value!);
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
