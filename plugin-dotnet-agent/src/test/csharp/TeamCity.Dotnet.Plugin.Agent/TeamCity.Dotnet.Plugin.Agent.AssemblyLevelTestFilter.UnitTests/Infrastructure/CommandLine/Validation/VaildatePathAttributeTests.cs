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

using System.IO.Abstractions;
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

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
        var pathMock = new Mock<IPath>();
        pathMock.Setup(m => m.GetFullPath(It.IsAny<string>())).Throws(new ArgumentException());
        _mockFileSystem.Setup(fs => fs.Path).Returns(pathMock.Object);

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
        var fileMock = new Mock<IFile>();
        fileMock.Setup(m => m.Exists(It.IsAny<string>())).Returns(false);
        _mockFileSystem.Setup(fs => fs.File).Returns(fileMock.Object);

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
        var fileMock = new Mock<IFile>();
        fileMock.Setup(m => m.Exists(It.IsAny<string>())).Returns(true);
        _mockFileSystem.Setup(fs => fs.File).Returns(fileMock.Object);
        var pathMock = new Mock<IPath>();
        pathMock.Setup(m => m.GetFullPath(It.IsAny<string>())).Returns(".docx");
        _mockFileSystem.Setup(fs => fs.Path).Returns(pathMock.Object);

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
        const string extension = ".txt";
        var attribute = new ValidatePathAttribute(_mockFileSystem.Object, true, true, "Invalid extension", extension);
        var fileMock = new Mock<IFile>();
        fileMock.Setup(m => m.Exists(It.IsAny<string>())).Returns(true);
        _mockFileSystem.Setup(fs => fs.File).Returns(fileMock.Object);
        var pathMock = new Mock<IPath>();
        pathMock.Setup(m => m.GetFullPath(It.IsAny<string>())).Returns(extension);
        _mockFileSystem.Setup(fs => fs.Path).Returns(pathMock.Object);
        _mockFileSystem.Setup(m => m.Path.GetExtension(It.IsAny<string>())).Returns(extension);

        // act
        var result = attribute.IsValid("C:\\path\\to\\file.txt");

        // assert
        Assert.True(result.IsValid);
    }
}
