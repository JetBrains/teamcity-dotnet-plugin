using System.IO.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

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

    public override ValidationResult IsValid(object value)
    {
        if (string.IsNullOrEmpty(value.ToString()))
        {
            return ValidationResult.Invalid(ErrorMessage);
        }

        var path = value.ToString();
        
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

