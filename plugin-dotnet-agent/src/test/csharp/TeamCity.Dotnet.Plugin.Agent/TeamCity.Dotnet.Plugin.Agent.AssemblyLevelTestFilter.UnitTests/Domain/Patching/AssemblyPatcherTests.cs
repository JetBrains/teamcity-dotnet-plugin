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

using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.Patching;

public class AssemblyPatcherTests
{
    private readonly Mock<IFileSystem> _fileSystemMock;
    private readonly Mock<IAssemblyLoader> _assemblyLoader;
    private readonly Mock<ILogger<AssemblyPatcher>> _loggerMock;
    private readonly TestPatchingCriteria _testCriteria = new();
    private readonly TestMutator _mutator;
    private readonly IAssemblyPatcher _assemblyPatcher;

    public AssemblyPatcherTests()
    {
        _fileSystemMock = new Mock<IFileSystem>();
        _assemblyLoader = new Mock<IAssemblyLoader>();
        _loggerMock = new Mock<ILogger<AssemblyPatcher>>();
        _mutator = new TestMutator();
        _assemblyPatcher = new AssemblyPatcher(
            new[] { _mutator }, _fileSystemMock.Object, _assemblyLoader.Object, _loggerMock.Object);
    }
    
    [Fact]
    public async Task TryPatchAsync_MutationNotApplied_AssemblyNotPatched()
    {
        // arrange
        var assemblyFile = new FileInfo("assembly.dll");
        _mutator.Result = new AssemblyMutationResult(0,0);
    
        // act
        var result = await _assemblyPatcher.TryPatchAsync(assemblyFile, _testCriteria);
    
        // assert
        Assert.False(result.IsAssemblyPatched);
        Assert.Equal(assemblyFile.FullName, result.AssemblyPath);
        Assert.Equal(string.Empty, result.BackupAssemblyPath);
        Assert.Null(result.SymbolsPath);
        Assert.Null(result.BackupSymbolsPath);
        Assert.Equal(AssemblyMutationResult.Empty, result.MutationResult);
    }

    [Fact]
    public async Task TryPatchAsync__MutationApplied_NoSymbols__AssemblyPatched()
    {
        // arrange
        var assemblyFile = new FileInfo("assembly.dll");
        var assemblyMock = new Mock<IDotnetAssembly>();
        _assemblyLoader.
            Setup(m => m.LoadAssembly(It.IsAny<string>(), false))
            .Returns(assemblyMock.Object);
        _mutator.Result = new AssemblyMutationResult(1,1);
    
        // act
        var result = await _assemblyPatcher.TryPatchAsync(assemblyFile, _testCriteria);
    
        // assert
        Assert.True(result.IsAssemblyPatched);
        Assert.Equal(assemblyFile.FullName, result.AssemblyPath);
        Assert.Equal(assemblyFile.FullName + "_backup", result.BackupAssemblyPath);
        Assert.Null(result.SymbolsPath);
        Assert.Null(result.BackupSymbolsPath);
        Assert.Equal(_mutator.Result, result.MutationResult);
        _fileSystemMock.Verify(m => m.CopyFile(assemblyFile.FullName, assemblyFile.FullName + "_backup"), Times.Once);
        _fileSystemMock.Verify(m => m.CopyFile(assemblyFile.FullName, assemblyFile.FullName + "_tmp"), Times.Once);
        assemblyMock.Verify(m => m.Write(It.IsAny<FileStream>(), false), Times.Once);
        _fileSystemMock.Verify(m => m.DeleteFile(assemblyFile.FullName), Times.Once);
        _fileSystemMock.Verify(m => m.MoveFile(assemblyFile.FullName + "_tmp", assemblyFile.FullName), Times.Once);
    }
    
    [Fact]
    public async Task TryPatchAsync__MutationApplied_WithSymbols__AssemblyPatched()
    {
        // arrange
        var assemblyFile = new FileInfo("assembly.dll");
        var assemblyMock = new Mock<IDotnetAssembly>();
        assemblyMock.Setup(m => m.HasSymbols).Returns(true);
        _assemblyLoader.
            Setup(m => m.LoadAssembly(It.IsAny<string>(), true))
            .Returns(assemblyMock.Object);
        _fileSystemMock.Setup(m => m.FileExists(It.IsAny<string>())).Returns(true);
        _fileSystemMock.Setup(m => m.ChangeFileExtension(It.IsAny<string>(), FileExtension.Symbols))
            .Returns((string path, string _) => path[..^4] + FileExtension.Symbols);
        _mutator.Result = new AssemblyMutationResult(1,1);
    
        // act
        var result = await _assemblyPatcher.TryPatchAsync(assemblyFile, _testCriteria);
    
        // assert
        Assert.True(result.IsAssemblyPatched);
        Assert.Equal(assemblyFile.FullName, result.AssemblyPath);
        Assert.Equal(assemblyFile.FullName + "_backup", result.BackupAssemblyPath);
        var symbolsPath = assemblyFile.FullName[..^4] + FileExtension.Symbols;
        Assert.Equal(symbolsPath, result.SymbolsPath);
        Assert.Equal(symbolsPath + "_backup", result.BackupSymbolsPath);
        Assert.Equal(_mutator.Result, result.MutationResult);
        _fileSystemMock.Verify(m => m.CopyFile(assemblyFile.FullName, assemblyFile.FullName + "_backup"), Times.Once);
        _fileSystemMock.Verify(m => m.CopyFile(assemblyFile.FullName, assemblyFile.FullName + "_tmp"), Times.Once);
        _fileSystemMock.Verify(m => m.CopyFile(symbolsPath, symbolsPath + "_backup"), Times.Once);
        assemblyMock.Verify(m => m.Write(It.IsAny<FileStream>(), true), Times.Once);
        _fileSystemMock.Verify(m => m.DeleteFile(assemblyFile.FullName), Times.Once);
        _fileSystemMock.Verify(m => m.MoveFile(assemblyFile.FullName + "_tmp", assemblyFile.FullName), Times.Once);
    }
    
    internal class TestPatchingCriteria : IAssemblyPatchingCriteria {}
    
    internal class TestMutator : IAssemblyMutator<TestPatchingCriteria>
    {
        internal AssemblyMutationResult? Result { get; set; }
        
        public Task<AssemblyMutationResult?> MutateAsync(IDotnetAssembly assembly, TestPatchingCriteria criteria) =>
            Task.FromResult(Result);

        public Task<AssemblyMutationResult?> MutateAsync(IDotnetAssembly assembly, IAssemblyPatchingCriteria criteria) =>
            Task.FromResult(Result);
    }
}
