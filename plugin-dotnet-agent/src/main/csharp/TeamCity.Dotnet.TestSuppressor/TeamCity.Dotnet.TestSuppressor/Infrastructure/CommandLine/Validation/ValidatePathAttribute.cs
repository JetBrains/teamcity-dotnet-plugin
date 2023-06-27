using System.IO.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;

internal class ValidatePathAttribute : ValidationAttribute
{
    private readonly bool _mustBeFile;
    private readonly bool _mustExist;
    private readonly IEnumerable<string> _allowedExtensions;
    private readonly IFileSystem _fileSystem;

    public ValidatePathAttribute(bool mustBeFile, bool mustExist, string errorMessage, params string[] allowedExtensions)
        : base(errorMessage)
    {
        _mustBeFile = mustBeFile;
        _mustExist = mustExist;
        _allowedExtensions = allowedExtensions;
        _fileSystem = new FileSystem();
    }
    
    public ValidatePathAttribute(IFileSystem fileSystem, bool mustBeFile, bool mustExist, string errorMessage, params string[] allowedExtensions)
        : base(errorMessage)
    {
        _fileSystem = fileSystem;
        _mustBeFile = mustBeFile;
        _mustExist = mustExist;
        _allowedExtensions = allowedExtensions;
    }

    public override ValidationResult Validate(object value)
    {
        if (value is not Array array)
        {
            return Validate(value as string);
        }
        
        foreach (var item in array)
        {
            var validationResult = Validate(item as string);
            if (!validationResult.IsValid)
            {
                return validationResult;
            }
        }
        return ValidationResult.Valid;

    }

    private ValidationResult Validate(string? path)
    {
        if (string.IsNullOrEmpty(path))
        {
            return ValidationResult.Invalid(ErrorMessage);
        }

        // check if string is valid path
        try
        {
            _ = _fileSystem.Path.GetFullPath(path!);
        }
        catch (Exception ex)
        {
            return ValidationResult.Invalid($"{ErrorMessage}: {path} – this string is not a valid path: {ex.Message}");
        }

        if (!_mustExist)
        {
            return ValidationResult.Valid;
        }
        
        if (_mustBeFile)
        {
            if (!_fileSystem.File.Exists(path!))
            {
                return ValidationResult.Invalid($"{ErrorMessage}: {path} – file does not exist");
            }
                
            if (_allowedExtensions.Any())
            {
                var fileExtension = _fileSystem.Path.GetExtension(path!);
                if (!_allowedExtensions.Contains(fileExtension, StringComparer.OrdinalIgnoreCase))
                {
                    return ValidationResult.Invalid($"{ErrorMessage}: invalid file extension for path {path}");
                }
            }
        }
        else
        {
            if (!_fileSystem.File.Exists(path!) && !_fileSystem.Directory.Exists(path!))
            {
                return ValidationResult.Invalid($"{ErrorMessage}: {path} – file/directory does not exist");
            }
        }

        return ValidationResult.Valid;
    }
}

