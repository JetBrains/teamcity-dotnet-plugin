using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.Targeting.Strategies;
using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.Targeting.Strategies;

public class AssemblyTargetResolvingStrategyTests
{
    private readonly Mock<IFileSystem> _fileSystemMock;
    private readonly AssemblyTargetResolvingStrategy _strategy;
    private readonly Mock<IDotnetAssemblyLoader> _assemblyLoaderMock;

    public AssemblyTargetResolvingStrategyTests()
    {
        var logger = Mock.Of<ILogger<AssemblyTargetResolvingStrategy>>();
        _fileSystemMock = new Mock<IFileSystem>();
        _assemblyLoaderMock = new Mock<IDotnetAssemblyLoader>();

        var testEngines = new List<ITestEngine>
        {
            new TestTestEngine1(),
            new TestTestEngine2(),
        };

        _strategy = new AssemblyTargetResolvingStrategy(_fileSystemMock.Object, logger, testEngines, _assemblyLoaderMock.Object);
    }

    [Fact]
    public void Resolve_ShouldYieldBreak_WhenFileDoesNotExist()
    {
        // arrange
        const string target = "invalid";
        _fileSystemMock.Setup(fs => fs.File.Exists(target)).Returns(false);

        // act
        var result = _strategy.Resolve(target);

        // assert
        Assert.Empty(result);
    }

    [Fact]
    public void Resolve_ShouldYieldBreak_WhenUnsupportedExtension()
    {
        // arrange
        const string target = "target.unsupported";
        var targetFileInfoMock = new Mock<IFileInfo>();
        targetFileInfoMock.Setup(m => m.FullName).Returns(target);
        _fileSystemMock.Setup(fs => fs.File.Exists(target)).Returns(true);
        _fileSystemMock
            .Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>()))
            .Returns(targetFileInfoMock.Object);
    
        // act
        var result = _strategy.Resolve(target);
    
        // assert
        Assert.Empty(result);
    }
    
    [Fact]
    public void Resolve_ShouldYieldBreak_WhenNotNetAssembly()
    {
        // arrange
        const string target = "target.dll";
        var targetFileInfoMock = new Mock<IFileInfo>();
        targetFileInfoMock.Setup(m => m.FullName).Returns(target);
        targetFileInfoMock.Setup(fs => fs.Exists).Returns(true);
        targetFileInfoMock.Setup(fs => fs.Extension).Returns(".dll");
        _fileSystemMock
            .Setup(m => m.DirectoryInfo.Wrap(It.IsAny<DirectoryInfo>()))
            .Returns(Mock.Of<IDirectoryInfo>());
        _fileSystemMock
            .Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>()))
            .Returns(targetFileInfoMock.Object);
        _assemblyLoaderMock.Setup(a => a.LoadAssembly(target, false)).Returns(default(IDotnetAssembly));
    
        // act
        var result = _strategy.Resolve(target);
    
        // assert
        Assert.Empty(result);
    }
    
    [Fact]
    public void Resolve_ShouldYieldBreak_WhenNoSupportedTestFrameworks()
    {
        // arrange
        const string target = "target.dll";
        var targetFileInfoMock = new Mock<IFileInfo>();
        targetFileInfoMock.Setup(m => m.FullName).Returns(target);
        targetFileInfoMock.Setup(fs => fs.Exists).Returns(true);
        targetFileInfoMock.Setup(fs => fs.Extension).Returns(".dll");
        _fileSystemMock
            .Setup(m => m.DirectoryInfo.Wrap(It.IsAny<DirectoryInfo>()))
            .Returns(Mock.Of<IDirectoryInfo>());
        _fileSystemMock
            .Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>()))
            .Returns(targetFileInfoMock.Object);
        var assemblyMock = new Mock<IDotnetAssembly>();
        _assemblyLoaderMock.Setup(a => a.LoadAssembly(target, false)).Returns(assemblyMock.Object);
    
        // act
        var result = _strategy.Resolve(target);
    
        // assert
        Assert.Empty(result);
    }
    
    [Fact]
    public void Resolve_ShouldReturnResolvedAssembly_WhenSupportedTestFrameworks()
    {
        // arrange
        const string target = "target.dll";
        var targetFileInfoMock = new Mock<IFileInfo>();
        targetFileInfoMock.Setup(m => m.FullName).Returns(target);
        targetFileInfoMock.Setup(fs => fs.Exists).Returns(true);
        targetFileInfoMock.Setup(fs => fs.Extension).Returns(".dll");
        _fileSystemMock
            .Setup(m => m.DirectoryInfo.Wrap(It.IsAny<DirectoryInfo>()))
            .Returns(Mock.Of<IDirectoryInfo>());
        _fileSystemMock
            .Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>()))
            .Returns(targetFileInfoMock.Object);
        var assemblyMock = new Mock<IDotnetAssembly>();
        var dotnetAssemblyReferenceMock = new Mock<IDotnetAssemblyReference>();
        dotnetAssemblyReferenceMock
            .Setup(m => m.FullName).Returns("assembly1");
        dotnetAssemblyReferenceMock
            .Setup(m => m.Name).Returns("assembly1");
        assemblyMock
            .Setup(m => m.AssemblyReferences)
            .Returns(new[] { dotnetAssemblyReferenceMock.Object });
        _assemblyLoaderMock
            .Setup(a => a.LoadAssembly(It.IsAny<string>(), false))
            .Returns(assemblyMock.Object);
    
        // act
        var result = _strategy.Resolve(target).ToList();
    
        // assert
        Assert.Single(result);
        Assert.Equal(target, result[0].Item1.FullName);
        Assert.Equal(_strategy.TargetType, result[0].Item2);
    }
    
    internal class TestTestEngine1 : ITestEngine
    {
        public string Name => "TestEngine1";
        public IEnumerable<string> AssemblyNames { get; } = new[] { "assembly1", "assembly2" };
        public IReadOnlyList<string> TestClassAttributes => new List<string>();
        public IReadOnlyList<string> TestMethodAttributes => new List<string>();
    }
    
    internal class TestTestEngine2 : ITestEngine
    {
        public string Name => "TestEngine2";
        public IEnumerable<string> AssemblyNames { get; } = new[] { "assembly3", "assembly4" };
        public IReadOnlyList<string> TestClassAttributes => new List<string>();
        public IReadOnlyList<string> TestMethodAttributes => new List<string>();
    }
}