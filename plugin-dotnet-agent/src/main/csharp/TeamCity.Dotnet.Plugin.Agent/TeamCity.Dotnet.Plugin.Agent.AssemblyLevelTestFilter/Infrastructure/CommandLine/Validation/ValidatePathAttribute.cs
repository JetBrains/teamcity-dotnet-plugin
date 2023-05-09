/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

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
        _fileSystem = new FileSystemWrapper();
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
            _ = _fileSystem.GetFullPath(path!);
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
            if (!_fileSystem.FileExists(path!))
            {
                return ValidationResult.Invalid($"{ErrorMessage}: {path} – file does not exist");
            }
                
            if (_allowedExtensions.Any())
            {
                var fileExtension = _fileSystem.GetExtension(path!);
                if (!_allowedExtensions.Contains(fileExtension, StringComparer.OrdinalIgnoreCase))
                {
                    return ValidationResult.Invalid($"{ErrorMessage}: invalid file extension for path {path}");
                }
            }
        }
        else
        {
            if (!_fileSystem.FileExists(path!) && !_fileSystem.DirectoryExists(path!))
            {
                return ValidationResult.Invalid($"{ErrorMessage}: {path} – file/directory does not exist");
            }
        }

        return ValidationResult.Valid;
    }
}

