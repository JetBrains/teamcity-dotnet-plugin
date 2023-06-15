namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

[AttributeUsage(AttributeTargets.Property)]
internal class RequiredAttribute : ValidationAttribute
{
    public override ValidationResult IsValid(object value) => value != null
        ? ValidationResult.Valid
        : ValidationResult.Invalid("The setting is required: " + ErrorMessage);

    public RequiredAttribute(string errorMessage) : base(errorMessage)
    {
    }
}
