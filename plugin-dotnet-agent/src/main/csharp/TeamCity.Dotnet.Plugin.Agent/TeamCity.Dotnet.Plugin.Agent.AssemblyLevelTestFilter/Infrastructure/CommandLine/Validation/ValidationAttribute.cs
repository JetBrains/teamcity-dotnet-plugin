namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

[AttributeUsage(AttributeTargets.Property, AllowMultiple = true)]
public abstract class ValidationAttribute : Attribute
{
    public string ErrorMessage { get; set; }

    protected ValidationAttribute(string errorMessage)
    {
        ErrorMessage = errorMessage;
    }

    public abstract ValidationResult IsValid(object value);
}