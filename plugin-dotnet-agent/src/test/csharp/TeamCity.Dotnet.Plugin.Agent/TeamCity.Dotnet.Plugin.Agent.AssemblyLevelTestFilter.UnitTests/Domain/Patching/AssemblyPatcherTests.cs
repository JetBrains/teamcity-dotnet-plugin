using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.Patching;

public class AssemblyPatcherTests
{
    private readonly Mock<IFileSystem> _fileSystemMock;
    private readonly Mock<IFileCopier> _fileCopierMock;
    private readonly Mock<IDotnetAssemblyLoader> _assemblyLoader;
    private readonly TestPatchingCriteria _testCriteria = new();
    private readonly TestMutator _mutator;
    private readonly IAssemblyPatcher _assemblyPatcher;

    public AssemblyPatcherTests()
    {
        _fileSystemMock = new Mock<IFileSystem>();
        _fileCopierMock = new Mock<IFileCopier>();
        _assemblyLoader = new Mock<IDotnetAssemblyLoader>();
        Mock<ILogger<AssemblyPatcher>> loggerMock = new();
        _mutator = new TestMutator();
        _assemblyPatcher = new AssemblyPatcher(
            new[] { _mutator }, _assemblyLoader.Object, _fileSystemMock.Object, _fileCopierMock.Object, loggerMock.Object);
    }
    
    [Fact]
    public async Task TryPatchAsync_MutationNotApplied_AssemblyNotPatched()
    {
        // arrange
        var assemblyPath = "assembly.dll";
        var symbolsPath = "assembly.pdb";
        var assemblyFileMock = new Mock<IFileInfo>();
        assemblyFileMock.Setup(m => m.FullName).Returns(assemblyPath);
        var fileMock = new Mock<IFile>();
        _fileSystemMock.Setup(m => m.File).Returns(fileMock.Object);
        var fileInfoFactoryMock = new Mock<IFileInfoFactory>();
        fileInfoFactoryMock.Setup(m => m.New(assemblyPath)).Returns(assemblyFileMock.Object);
        _mutator.Result = new AssemblyMutationResult(0,0);
        var assemblyMock = new Mock<IDotnetAssembly>();
        _assemblyLoader.
            Setup(m => m.LoadAssembly(It.IsAny<string>(), false))
            .Returns(assemblyMock.Object);
        _fileSystemMock
            .Setup(m => m.Path.ChangeExtension(It.IsAny<string>(), FileExtension.Symbols))
            .Returns(symbolsPath);
        _fileSystemMock
            .Setup(m => m.File.Exists(symbolsPath))
            .Returns(false);    // no symbols
    
        // act
        var result = await _assemblyPatcher.TryPatchAsync(assemblyFileMock.Object, _testCriteria);
    
        // assert
        Assert.False(result.IsAssemblyPatched);
        Assert.Equal(assemblyPath, result.AssemblyPath);
        Assert.Equal(string.Empty, result.BackupAssemblyPath);
        Assert.Null(result.SymbolsPath);
        Assert.Null(result.BackupSymbolsPath);
        Assert.Equal(AssemblyMutationResult.Empty, result.MutationResult);
    }

    [Fact]
    public async Task TryPatchAsync__MutationApplied_NoSymbols__AssemblyPatched()
    {
        // arrange
        var assemblyPath = "assembly.dll";
        var symbolsPath = "assembly.pdb";
        var assemblyFileMock = new Mock<IFileInfo>();
        assemblyFileMock.Setup(m => m.FullName).Returns(assemblyPath);
        
        var assemblyMock = new Mock<IDotnetAssembly>();
        _assemblyLoader.
            Setup(m => m.LoadAssembly(It.IsAny<string>(), false))
            .Returns(assemblyMock.Object);
        _mutator.Result = new AssemblyMutationResult(1,1);
        _fileSystemMock
            .Setup(m => m.Path.ChangeExtension(It.IsAny<string>(), FileExtension.Symbols))
            .Returns(symbolsPath);
        _fileSystemMock
            .Setup(m => m.File.Exists(symbolsPath))
            .Returns(false);    // no symbols
    
        // act
        var result = await _assemblyPatcher.TryPatchAsync(assemblyFileMock.Object, _testCriteria);
    
        // assert
        Assert.True(result.IsAssemblyPatched);
        Assert.Equal(assemblyPath, result.AssemblyPath);
        Assert.Equal(assemblyPath + "_backup", result.BackupAssemblyPath);
        Assert.Null(result.SymbolsPath);
        Assert.Null(result.BackupSymbolsPath);
        Assert.Equal(_mutator.Result, result.MutationResult);
        _fileCopierMock.Verify(m => m.CopyFile(assemblyPath, assemblyPath + "_backup"), Times.Once);
        _fileCopierMock.Verify(m => m.CopyFile(assemblyPath, assemblyPath + "_tmp"), Times.Once);
        assemblyMock.Verify(m => m.SaveTo(It.IsAny<string>(), false), Times.Once);
        _fileSystemMock.Verify(m => m.File.Delete(assemblyPath), Times.Once);
        _fileSystemMock.Verify(m => m.File.Move(assemblyPath + "_tmp", assemblyPath), Times.Once);
    }
    
    [Fact]
    public async Task TryPatchAsync__MutationApplied_WithSymbols__AssemblyPatched()
    {
        // arrange
        var assemblyPath = "assembly.dll";
        var symbolsPath = "assembly.pdb";
        var assemblyFileMock = new Mock<IFileInfo>();
        assemblyFileMock.Setup(m => m.FullName).Returns(assemblyPath);
        
        var assemblyMock = new Mock<IDotnetAssembly>();
        assemblyMock.Setup(m => m.HasSymbols).Returns(true);
        _assemblyLoader.
            Setup(m => m.LoadAssembly(It.IsAny<string>(), true))
            .Returns(assemblyMock.Object);
        _fileSystemMock.Setup(m => m.File.Exists(It.IsAny<string>())).Returns(true);
        _fileSystemMock.Setup(m => m.Path.ChangeExtension(It.IsAny<string>(), FileExtension.Symbols))
            .Returns((string path, string _) => path[..^4] + FileExtension.Symbols);
        _mutator.Result = new AssemblyMutationResult(1,1);
        _fileSystemMock
            .Setup(m => m.Path.ChangeExtension(It.IsAny<string>(), FileExtension.Symbols))
            .Returns(symbolsPath);
        _fileSystemMock
            .Setup(m => m.File.Exists(symbolsPath))
            .Returns(true);    // no symbols
    
        // act
        var result = await _assemblyPatcher.TryPatchAsync(assemblyFileMock.Object, _testCriteria);
    
        // assert
        Assert.True(result.IsAssemblyPatched);
        Assert.Equal(assemblyPath, result.AssemblyPath);
        Assert.Equal(assemblyPath + "_backup", result.BackupAssemblyPath);
        Assert.Equal(symbolsPath, result.SymbolsPath);
        Assert.Equal(symbolsPath + "_backup", result.BackupSymbolsPath);
        Assert.Equal(_mutator.Result, result.MutationResult);
        _fileCopierMock.Verify(m => m.CopyFile(assemblyPath, assemblyPath + "_backup"), Times.Once);
        _fileCopierMock.Verify(m => m.CopyFile(assemblyPath, assemblyPath + "_tmp"), Times.Once);
        _fileCopierMock.Verify(m => m.CopyFile(symbolsPath, symbolsPath + "_backup"), Times.Once);
        assemblyMock.Verify(m => m.SaveTo(It.IsAny<string>(), true), Times.Once);
        _fileSystemMock.Verify(m => m.File.Delete(assemblyPath), Times.Once);
        _fileSystemMock.Verify(m => m.File.Move(assemblyPath + "_tmp", assemblyPath), Times.Once);
    }
    
    internal class TestPatchingCriteria : IAssemblyPatchingCriteria {}
    
    internal class TestMutator : IAssemblyMutator<TestPatchingCriteria>
    {
        internal AssemblyMutationResult Result { get; set; } = AssemblyMutationResult.Empty!;
        
        public Task<AssemblyMutationResult> MutateAsync(IDotnetAssembly assembly, TestPatchingCriteria criteria) =>
            Task.FromResult(Result);

        public Task<AssemblyMutationResult> MutateAsync(IDotnetAssembly assembly, IAssemblyPatchingCriteria criteria) =>
            Task.FromResult(Result);
    }
}
