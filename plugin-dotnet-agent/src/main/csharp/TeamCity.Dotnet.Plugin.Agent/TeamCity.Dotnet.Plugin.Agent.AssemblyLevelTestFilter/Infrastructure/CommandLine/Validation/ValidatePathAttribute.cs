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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

internal class ValidatePathAttribute : ValidationAttribute
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
                    return ValidationResult.Invalid($"{ErrorMessage}: `{path}` file does not exist");
                case false when !Directory.Exists(path):
                    return ValidationResult.Invalid($"{ErrorMessage}: `{path}` directory does not exist");
            }
        }

        if (_mustBeFile && _allowedExtensions.Any())
        {
            var fileExtension = Path.GetExtension(path);
            if (!_allowedExtensions.Contains(fileExtension, StringComparer.OrdinalIgnoreCase))
            {
                return ValidationResult.Invalid($"{ErrorMessage}: invalid file extension for `{path}`");
            }
        }

        return ValidationResult.Valid;
    }
}

