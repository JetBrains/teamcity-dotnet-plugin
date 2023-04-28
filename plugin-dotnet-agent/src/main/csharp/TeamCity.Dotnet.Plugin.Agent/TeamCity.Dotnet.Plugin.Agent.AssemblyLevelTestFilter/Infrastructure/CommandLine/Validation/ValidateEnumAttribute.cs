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

[AttributeUsage(AttributeTargets.Property)]
internal class ValidateEnumAttribute : ValidationAttribute
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