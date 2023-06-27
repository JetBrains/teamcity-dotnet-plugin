using System.IO.Abstractions.TestingHelpers;
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.Targeting.Strategies;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.Targeting.Strategies;

public class SolutionTargetResolvingStrategyTests
{
    private readonly MockFileSystem _fileSystemMock;
    private readonly SolutionTargetResolvingStrategy _strategy;

    public SolutionTargetResolvingStrategyTests()
    {
        var logger = Mock.Of<ILogger<SolutionTargetResolvingStrategy>>();
        _fileSystemMock = new MockFileSystem();
        _strategy = new SolutionTargetResolvingStrategy(_fileSystemMock, logger);
    }

    [Fact]
    public void Resolve_ShouldNotReturnResult_WhenFileDoesNotExist()
    {
        // arrange, act
        var result = _strategy.Resolve("");

        // assert
        Assert.Empty(result);
    }
    
    [Fact]
    public void Resolve_ShouldNotReturnResult_WhenUnsupportedExtension()
    {
        // arrange
        const string projectPath = "solution.unsupported";
        _fileSystemMock.AddFile(projectPath, new MockFileData(""));
    
        // act
        var result = _strategy.Resolve(projectPath);
    
        // assert
        Assert.Empty(result);
    }
    
    [Fact]
    public void Resolve_ShouldNotReturnResult_WhenFileIsInvalidProject()
    {
        // arrange
        const string projectPath = "solution.sln";
        var projectFile = new MockFileData("INVALID");
        _fileSystemMock.AddFile(projectPath, projectFile);
    
        // act
        var result = _strategy.Resolve(projectPath);
    
        // assert
        Assert.Empty(result);
    }
    
    // all other scenarios should be covered by integration tests
    // since they require a real file system
}