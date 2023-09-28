using System.IO.Abstractions.TestingHelpers;
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.Targeting;
using TeamCity.Dotnet.TestSuppressor.Domain.Targeting.Strategies;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.Targeting.Strategies;

public class DirectoryTargetResolvingStrategyTests
{
    private readonly Mock<ILogger<DirectoryTargetResolvingStrategy>> _loggerMock = new();
    private readonly MockFileSystem _fileSystem = new();

    [Fact]
    public void TestResolve_NoSuchDirectory()
    {
        // arrange
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);

        // act
        var result = strategy.Resolve("/fake/directory".ToPlatformPath()).ToList();

        // assert
        Assert.Empty(result);
    }

    [Fact]
    public void TestResolve_ValidDirectory_NoProjectsOrSolutions()
    {
        // arrange
        _fileSystem.AddDirectory("/fake/directory".ToPlatformPath());
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);
            
        // act
        var result = strategy.Resolve("/fake/directory".ToPlatformPath()).ToList();

        // assert
        Assert.Empty(result);
    }

    [Fact]
    public void TestResolve_ValidDirectory_WithSolution()
    {
        // arrange
        _fileSystem.AddFile("/fake/directory/test.sln".ToPlatformPath(), new MockFileData(""));
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);
            
        // act
        var result = strategy.Resolve("/fake/directory".ToPlatformPath()).ToList();

        // assert
        Assert.Single(result);
        Assert.Equal("/fake/directory/test.sln".ToPlatformPath(), result.First().Item1.FullName);
        Assert.Equal(TargetType.Solution, result.First().Item2);
    }

    [Fact]
    public void TestResolve_ValidDirectory_WithProject()
    {
        // arrange
        _fileSystem.AddFile("/fake/directory/test.csproj".ToPlatformPath(), new MockFileData(""));
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);
            
        // act
        var result = strategy.Resolve("/fake/directory".ToPlatformPath()).ToList();

        // assert
        Assert.Single(result);
        Assert.Equal("/fake/directory/test.csproj".ToPlatformPath(), result.First().Item1.FullName);
        Assert.Equal(TargetType.Project, result.First().Item2);
    }

    [Fact]
    public void TestResolve_ValidDirectory_WithAssembly()
    {
        // arrange
        _fileSystem.AddFile("/fake/directory/test.dll".ToPlatformPath(), new MockFileData(""));
        var strategy = new DirectoryTargetResolvingStrategy(_fileSystem, _loggerMock.Object);
            
        // act
        var result = strategy.Resolve("/fake/directory".ToPlatformPath()).ToList();

        // assert
        Assert.Single(result);
        Assert.Equal("/fake/directory/test.dll".ToPlatformPath(), result.First().Item1.FullName);
        Assert.Equal(TargetType.Assembly, result.First().Item2);
    }
}