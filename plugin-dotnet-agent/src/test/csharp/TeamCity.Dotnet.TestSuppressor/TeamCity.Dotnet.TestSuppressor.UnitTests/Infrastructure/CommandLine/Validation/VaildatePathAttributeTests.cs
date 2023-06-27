using System.IO.Abstractions;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure.CommandLine.Validation;

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
