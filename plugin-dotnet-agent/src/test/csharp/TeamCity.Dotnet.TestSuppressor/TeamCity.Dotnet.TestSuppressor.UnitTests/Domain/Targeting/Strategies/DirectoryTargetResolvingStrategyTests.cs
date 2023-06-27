using System.IO.Abstractions.TestingHelpers;
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.Targeting;
using TeamCity.Dotnet.TestSuppressor.Domain.Targeting.Strategies;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.Targeting.Strategies;

public class DirectoryTargetResolvingStrategyTests
{
    private readonly Mock<ILogger<DirectoryTargetResolvingStrategy>> _loggerMock;
    private readonly MockFileSystem _fileSystem;

    public DirectoryTargetResolvingStrategyTests()
    {
        _loggerMock = new Mock<ILogger<DirectoryTargetResolvingStrategy>>();
        _fileSystem = new MockFileSystem();
    }

    [Fact]
    public void TestResolve_NoSuchDirectory()
    {
        // arrange
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);
        const string directoryPath = "/fake/directory";
            
        // act
        var result = strategy.Resolve(directoryPath).ToList();

        // assert
        Assert.Empty(result);
    }

    [Fact]
    public void TestResolve_ValidDirectory_NoProjectsOrSolutions()
    {
        // arrange
        _fileSystem.AddDirectory("/fake/directory");
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);
            
        // act
        var result = strategy.Resolve("/fake/directory").ToList();

        // assert
        Assert.Empty(result);
    }

    [Fact]
    public void TestResolve_ValidDirectory_WithSolution()
    {
        // arrange
        _fileSystem.AddFile("/fake/directory/test.sln", new MockFileData(""));
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);
            
        // act
        var result = strategy.Resolve("/fake/directory").ToList();

        // assert
        Assert.Single(result);
        Assert.Equal("/fake/directory/test.sln", result.First().Item1.FullName);
        Assert.Equal(TargetType.Solution, result.First().Item2);
    }

    [Fact]
    public void TestResolve_ValidDirectory_WithProject()
    {
        // arrange
        _fileSystem.AddFile("/fake/directory/test.csproj", new MockFileData(""));
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);
            
        // act
        var result = strategy.Resolve("/fake/directory").ToList();

        // assert
        Assert.Single(result);
        Assert.Equal("/fake/directory/test.csproj", result.First().Item1.FullName);
        Assert.Equal(TargetType.Project, result.First().Item2);
    }

    [Fact]
    public void TestResolve_ValidDirectory_WithAssembly()
    {
        // arrange
        _fileSystem.AddFile("/fake/directory/test.dll", new MockFileData(""));
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);
            
        // act
        var result = strategy.Resolve("/fake/directory").ToList();

        // assert
        Assert.Single(result);
        Assert.Equal("/fake/directory/test.dll", result.First().Item1.FullName);
        Assert.Equal(TargetType.Assembly, result.First().Item2);
    }
}