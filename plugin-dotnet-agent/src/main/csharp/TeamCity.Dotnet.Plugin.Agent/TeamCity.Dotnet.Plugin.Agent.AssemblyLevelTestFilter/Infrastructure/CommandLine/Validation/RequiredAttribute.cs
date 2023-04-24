namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

public class RequiredAttribute : ValidationAttribute
{
    public override ValidationResult IsValid(object value)
    {
        var isValid = value != null;

        if (!isValid)
        {
            ErrorMessage = "The setting is required";
        }

        return ValidationResult.Valid;
    }

    public RequiredAttribute(string errorMessage) : base(errorMessage)
    {
    }
}