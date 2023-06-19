using System.IO.Abstractions.TestingHelpers;
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.MsBuild;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.Targeting.Strategies;

public class ProjectTargetResolvingStrategyTests
{
    private readonly MockFileSystem _fileSystemMock;
    private readonly ProjectTargetResolvingStrategy _strategy;

    public ProjectTargetResolvingStrategyTests()
    {
        var logger = Mock.Of<ILogger<ProjectTargetResolvingStrategy>>();
        _fileSystemMock = new MockFileSystem();
        _strategy = new ProjectTargetResolvingStrategy(_fileSystemMock, Mock.Of<IMsBuildLocator>(), logger);
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
        const string projectPath = "project.unsupported";
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
        const string projectPath = "project.csproj";
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