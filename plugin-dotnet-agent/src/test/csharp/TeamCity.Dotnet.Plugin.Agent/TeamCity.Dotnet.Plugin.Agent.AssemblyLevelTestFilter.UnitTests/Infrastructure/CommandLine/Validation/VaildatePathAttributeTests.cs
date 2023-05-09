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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure.CommandLine.Validation;

public class ValidatePathAttributeTests
{
    private readonly Mock<IFileSystem> _mockFileSystem;

    public ValidatePathAttributeTests()
    {
        _mockFileSystem = new Mock<IFileSystem>();
    }

    [Fact]
    public void IsValid_WithInvalidPath_ShouldReturnInvalidResult()
    {
        // arrange
        var attribute = new ValidatePathAttribute(_mockFileSystem.Object, false, false, "Invalid path");
        _mockFileSystem.Setup(fs => fs.GetFullPath(It.IsAny<string>())).Throws(new ArgumentException());

        // act
        var result = attribute.IsValid("Invalid\\Path");

        // assert
        Assert.False(result.IsValid);
        Assert.Contains("Invalid path", result.ErrorMessage);
    }

    [Fact]
    public void IsValid_WithMissingFile_ShouldReturnInvalidResult()
    {
        // arrange
        var attribute = new ValidatePathAttribute(_mockFileSystem.Object, true, true, "Missing file");
        _mockFileSystem.Setup(fs => fs.FileExists(It.IsAny<string>())).Returns(false);

        // act
        var result = attribute.IsValid("C:\\path\\to\\missing\\file.txt");

        // assert
        Assert.False(result.IsValid);
        Assert.Contains("Missing file", result.ErrorMessage);
    }

    [Fact]
    public void IsValid_WithInvalidExtension_ShouldReturnInvalidResult()
    {
        // arrange
        var attribute = new ValidatePathAttribute(_mockFileSystem.Object, true, true, "Invalid extension", ".txt");
        _mockFileSystem.Setup(fs => fs.FileExists(It.IsAny<string>())).Returns(true);
        _mockFileSystem.Setup(fs => fs.GetExtension(It.IsAny<string>())).Returns(".docx");

        // act
        var result = attribute.IsValid("C:\\path\\to\\file.docx");

        // assert
        Assert.False(result.IsValid);
        Assert.Contains("Invalid extension", result.ErrorMessage);
    }

    [Fact]
    public void IsValid_WithValidFile_ShouldReturnValidResult()
    {
        // arrange
        var attribute = new ValidatePathAttribute(_mockFileSystem.Object, true, true, "Invalid extension", ".txt");
        _mockFileSystem.Setup(fs => fs.FileExists(It.IsAny<string>())).Returns(true);
        _mockFileSystem.Setup(fs => fs.GetExtension(It.IsAny<string>())).Returns(".txt");

        // act
        var result = attribute.IsValid("C:\\path\\to\\file.txt");

        // assert
        Assert.True(result.IsValid);
    }
}
