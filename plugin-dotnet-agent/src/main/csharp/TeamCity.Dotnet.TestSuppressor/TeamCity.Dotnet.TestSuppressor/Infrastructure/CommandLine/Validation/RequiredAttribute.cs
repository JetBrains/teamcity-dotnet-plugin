namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;

[AttributeUsage(AttributeTargets.Property)]
internal class RequiredAttribute : ValidationAttribute
{
    public override ValidationResult Validate(object? value) =>
        value != null && (IsValidAsArray(value) || IsValidAsString(value))
            ? ValidationResult.Valid
            : ValidationResult.Invalid("The setting is required: " + ErrorMessage);

    public RequiredAttribute(string errorMessage) : base(errorMessage)
    {
    }

    private static bool IsValidAsArray(object value)
    {
        if (value is not Array array)
        {
            return true;
        }
        return array.Length == 0;
    }

    private static bool IsValidAsString(object value) =>
        value is not string str || string.IsNullOrWhiteSpace(str);
}
