namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

[AttributeUsage(AttributeTargets.Property)]
public class ValidateEnumAttribute : ValidationAttribute
{
    private readonly Type _enumType;
    
    public ValidateEnumAttribute(Type enumType, string errorMessage = "") : base(errorMessage)
    {
        if (!enumType.IsEnum)
        {
            throw new ArgumentException("Type must be an enum", nameof(enumType));
        }

        _enumType = enumType;
        ErrorMessage = $"Value must be one of the following: {string.Join(", ", Enum.GetNames(enumType))}";
    }
    
    public override ValidationResult IsValid(object value) => Enum.IsDefined(_enumType, value)
        ? ValidationResult.Valid
        : ValidationResult.Invalid(ErrorMessage);
}