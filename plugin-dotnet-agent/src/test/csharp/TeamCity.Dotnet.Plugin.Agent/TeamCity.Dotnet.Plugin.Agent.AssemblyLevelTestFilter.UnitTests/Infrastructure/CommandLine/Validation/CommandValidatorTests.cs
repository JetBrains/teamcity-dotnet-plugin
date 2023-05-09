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

using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Parsing;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure.CommandLine.Validation;

public class CommandValidatorTests
{
    [Fact]
    public void TestValidate_WithUnknownParameters_ShouldReturnInvalidResult()
    {
        // arrange
        var mockParsingResult = new Mock<IConfigurationParsingResult>();
        mockParsingResult.Setup(m => m.UnknownParameters).Returns(new List<string> { "unknown1", "unknown2" });
        var validator = new CommandValidator(mockParsingResult.Object);

        // act
        var result = validator.Validate(new TestCommand());

        // assert
        Assert.False(result.IsValid);
        Assert.Equal("Unknown arguments: unknown1, unknown2", result.ErrorMessage);
    }

    [Fact]
    public void TestValidate_WithNoUnknownParameters_ButInvalidProperties_ShouldReturnInvalidResult()
    {
        // arrange
        var mockParsingResult = new Mock<IConfigurationParsingResult>();
        mockParsingResult.Setup(m => m.UnknownParameters).Returns(new List<string>());
        var validator = new CommandValidator(mockParsingResult.Object);
        var testCommand = new TestCommand { Option = false }; // invalid value

        // act
        var result = validator.Validate(testCommand);

        // assert
        Assert.False(result.IsValid);
        Assert.Contains("Test error message", result.ErrorMessage);
    }

    [Fact]
    public void TestValidate_WithNoUnknownParameters_AndValidProperties_ShouldReturnValidResult()
    {
        // arrange
        var mockParsingResult = new Mock<IConfigurationParsingResult>();
        mockParsingResult.Setup(m => m.UnknownParameters).Returns(new List<string>());
        var validator = new CommandValidator(mockParsingResult.Object);
        var testCommand = new TestCommand { Option = true }; // valid value

        // act
        var result = validator.Validate(testCommand);

        // assert
        Assert.True(result.IsValid);
        Assert.Empty(result.ErrorMessage);
    }


    private class TestCommand : Command
    {
        [TestValidation(errorMessage: "Test error message")]
        public bool Option { get; set; }
    }
    
    [AttributeUsage(AttributeTargets.Property, AllowMultiple = true)]
    private class TestValidationAttribute : ValidationAttribute
    {
        public TestValidationAttribute(string errorMessage) : base(errorMessage)
        {
        }

        public override ValidationResult IsValid(object value)
        {
            if (value == null) throw new ArgumentNullException(nameof(value));
            return value is true ? ValidationResult.Valid : ValidationResult.Invalid(ErrorMessage);
        }
    }
}
