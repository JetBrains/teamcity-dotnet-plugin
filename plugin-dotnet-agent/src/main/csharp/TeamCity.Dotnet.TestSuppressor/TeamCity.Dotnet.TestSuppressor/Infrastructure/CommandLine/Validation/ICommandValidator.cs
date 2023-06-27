using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;

internal interface ICommandValidator
{
    ValidationResult Validate(Command command);
}