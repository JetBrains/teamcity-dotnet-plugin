namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

public class ValidatePathAttribute : ValidationAttribute
{
    private readonly bool _mustBeFile;
    private readonly bool _mustExist;
    private readonly IEnumerable<string> _allowedExtensions;

    public ValidatePathAttribute(bool mustBeFile, bool mustExist, string errorMessage, params string[] allowedExtensions)
        : base(errorMessage)
    {
        _mustBeFile = mustBeFile;
        _mustExist = mustExist;
        _allowedExtensions = allowedExtensions;
    }

    public override ValidationResult IsValid(object value)
    {
        if (string.IsNullOrEmpty(value.ToString()))
        {
            return ValidationResult.Invalid(ErrorMessage);
        }

        var path = value.ToString();

        if (_mustExist)
        {
            switch (_mustBeFile)
            {
                case true when !File.Exists(path):
                    return ValidationResult.Invalid($"{path} file does not exist: {ErrorMessage}");
                case false when !Directory.Exists(path):
                    return ValidationResult.Invalid($"{path} directory does not exist: {ErrorMessage}");
            }
        }

        if (_mustBeFile && _allowedExtensions.Any())
        {
            var fileExtension = Path.GetExtension(path);
            if (!_allowedExtensions.Contains(fileExtension, StringComparer.OrdinalIgnoreCase))
            {
                return ValidationResult.Invalid($"Invalid file extension for {path}: {ErrorMessage}");
            }
        }

        return ValidationResult.Valid;
    }
}

