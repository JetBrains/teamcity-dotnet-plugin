namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

[AttributeUsage(AttributeTargets.Property, AllowMultiple = true)]
internal abstract class ValidationAttribute : Attribute
{
    public string ErrorMessage { get; set; }

    protected ValidationAttribute(string errorMessage)
    {
        ErrorMessage = errorMessage;
    }

    public abstract ValidationResult IsValid(object value);
}