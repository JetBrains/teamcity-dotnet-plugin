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
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.Targeting;

public class TargetResolverTests
{
    private readonly Mock<IFileSystem> _fileSystemMock;
    private readonly TargetResolver _targetResolver;
    private readonly Mock<ITargetResolvingStrategy> _assemblyStrategy;
    private readonly Mock<ITargetResolvingStrategy> _projectStrategy;
    private readonly Mock<ITargetResolvingStrategy> _solutionStrategy;
    private readonly Mock<ITargetResolvingStrategy> _directoryStrategy;

    public TargetResolverTests()
    {
        var logger = Mock.Of<ILogger<TargetResolver>>();
        _fileSystemMock = new Mock<IFileSystem>();

        _assemblyStrategy = new Mock<ITargetResolvingStrategy>();
        _assemblyStrategy.Setup(m => m.TargetType).Returns(TargetType.Assembly);
        _projectStrategy = new Mock<ITargetResolvingStrategy>();
        _projectStrategy.Setup(m => m.TargetType).Returns(TargetType.Project);
        _solutionStrategy = new Mock<ITargetResolvingStrategy>();
        _solutionStrategy.Setup(m => m.TargetType).Returns(TargetType.Solution);
        _directoryStrategy = new Mock<ITargetResolvingStrategy>();
        _directoryStrategy.Setup(m => m.TargetType).Returns(TargetType.Directory);

        var strategies = new List<ITargetResolvingStrategy>
        {
            _assemblyStrategy.Object,
            _projectStrategy.Object,
            _solutionStrategy.Object,
            _directoryStrategy.Object
        };

        _targetResolver = new TargetResolver(strategies, _fileSystemMock.Object, logger);
    }

    [Fact]
    public void Resolve_ShouldThrowFileNotFoundException_WhenTargetNotExists()
    {
        // arrange
        _fileSystemMock.Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>())).Throws<Exception>();

        // act, assert
        Assert.Throws<FileNotFoundException>(() => _targetResolver.Resolve("").ToList());
    }

    [Fact]
    public void Resolve_ShouldThrowNotSupportedException_WhenUnsupportedTargetType()
    {
        // arrange
        const string target = "target.unknown";
        var fileInfoMock = new Mock<IFileInfo>();
        var directoryInfoMock = new Mock<IDirectoryInfo>();
        fileInfoMock.Setup(m => m.Exists).Returns(true);
        fileInfoMock.Setup(m => m.FullName).Returns(target);
        fileInfoMock.Setup(m => m.Extension).Returns(".unknown");
        directoryInfoMock.Setup(m => m.Exists).Returns(false);
        _fileSystemMock
            .Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>()))
            .Returns(fileInfoMock.Object);
        _fileSystemMock
            .Setup(fs => fs.DirectoryInfo.Wrap(It.IsAny<DirectoryInfo>()))
            .Returns(directoryInfoMock.Object);

        // act, assert
        Assert.Throws<NotSupportedException>(() => _targetResolver.Resolve(target).ToList());
    }

    [Fact]
    public void Resolve_ShouldResolve_WhenTargetIsAssembly()
    {
        // arrange
        const string target = "target.dll";
        var targetFileInfoMock = new Mock<IFileInfo>();
        var directoryInfoMock = new Mock<IDirectoryInfo>();
        targetFileInfoMock.Setup(m => m.Exists).Returns(true);
        targetFileInfoMock.Setup(m => m.FullName).Returns(target);
        targetFileInfoMock.Setup(m => m.Extension).Returns(".dll");
        directoryInfoMock.Setup(m => m.Exists).Returns(false);
        _fileSystemMock
            .Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>()))
            .Returns(targetFileInfoMock.Object);
        _fileSystemMock
            .Setup(fs => fs.DirectoryInfo.Wrap(It.IsAny<DirectoryInfo>()))
            .Returns(directoryInfoMock.Object);

        _assemblyStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (targetFileInfoMock.Object, TargetType.Assembly) });

        // act
        var result = _targetResolver.Resolve(target);

        // assert
        Assert.Single(result);
        _assemblyStrategy.Verify(s => s.Resolve(target), Times.Once);
    }

    [Fact]
    public void Resolve_ShouldResolve_WhenTargetIsProject()
    {
        // arrange
        const string target = "target.csproj";
        var targetFileInfoMock = new Mock<IFileInfo>();
        var directoryInfoMock = new Mock<IDirectoryInfo>();
        targetFileInfoMock.Setup(m => m.Exists).Returns(true);
        targetFileInfoMock.Setup(m => m.FullName).Returns(target);
        targetFileInfoMock.Setup(m => m.Extension).Returns(".csproj");
        directoryInfoMock.Setup(m => m.Exists).Returns(false);
        _fileSystemMock
            .Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>()))
            .Returns(targetFileInfoMock.Object);
        _fileSystemMock
            .Setup(fs => fs.DirectoryInfo.Wrap(It.IsAny<DirectoryInfo>()))
            .Returns(directoryInfoMock.Object);

        const string resolvedAssemblyPath = "assembly.dll";
        var resolvedAssemblyMock = new Mock<IFileInfo>();
        resolvedAssemblyMock.Setup(m => m.Exists).Returns(true);
        resolvedAssemblyMock.Setup(m => m.FullName).Returns(resolvedAssemblyPath);
    
        _projectStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (resolvedAssemblyMock.Object, TargetType.Assembly) });
        _assemblyStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (resolvedAssemblyMock.Object, TargetType.Assembly) });
    
        // act
        var result = _targetResolver.Resolve(target);
    
        // assert
        Assert.Single(result);
        _projectStrategy.Verify(s => s.Resolve(target), Times.Once);
        _assemblyStrategy.Verify(s => s.Resolve(resolvedAssemblyPath), Times.Once);
    }
    
    [Fact]
    public void Resolve_ShouldResolve_WhenTargetIsSolution()
    {
        // arrange
        const string target = "target.sln";
        var targetFileInfoMock = new Mock<IFileInfo>();
        var directoryInfoMock = new Mock<IDirectoryInfo>();
        targetFileInfoMock.Setup(m => m.Exists).Returns(true);
        targetFileInfoMock.Setup(m => m.FullName).Returns(target);
        targetFileInfoMock.Setup(m => m.Extension).Returns(".sln");
        directoryInfoMock.Setup(m => m.Exists).Returns(false);
        _fileSystemMock
            .Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>()))
            .Returns(targetFileInfoMock.Object);
        _fileSystemMock
            .Setup(fs => fs.DirectoryInfo.Wrap(It.IsAny<DirectoryInfo>()))
            .Returns(directoryInfoMock.Object);
        
        const string resolvedProjectPath = "project.csproj";
        var resolvedProjectMock = new Mock<IFileInfo>();
        resolvedProjectMock.Setup(m => m.Exists).Returns(true);
        resolvedProjectMock.Setup(m => m.FullName).Returns(resolvedProjectPath);

        const string resolvedAssemblyPath = "assembly.dll";
        var resolvedAssemblyMock = new Mock<IFileInfo>();
        resolvedAssemblyMock.Setup(m => m.Exists).Returns(true);
        resolvedAssemblyMock.Setup(m => m.FullName).Returns(resolvedAssemblyPath);
    
        _solutionStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (resolvedProjectMock.Object, TargetType.Project) });
        _projectStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (resolvedAssemblyMock.Object, TargetType.Assembly) });
        _assemblyStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (resolvedAssemblyMock.Object, TargetType.Assembly) });
    
        // act
        var result = _targetResolver.Resolve(target);
    
        // assert
        Assert.Single(result);
        _solutionStrategy.Verify(s => s.Resolve(target), Times.Once);
        _projectStrategy.Verify(s => s.Resolve(resolvedProjectPath), Times.Once);
        _assemblyStrategy.Verify(s => s.Resolve(resolvedAssemblyPath), Times.Once);
    }
    
    [Fact]
    public void Resolve_ShouldResolve_WhenTargetIsDirectory()
    {
        // arrange
        const string target = "target";
        var targetFileInfoMock = new Mock<IFileInfo>();
        var directoryInfoMock = new Mock<IDirectoryInfo>();
        targetFileInfoMock.Setup(m => m.Exists).Returns(false);
        directoryInfoMock.Setup(m => m.Exists).Returns(true);
        directoryInfoMock.Setup(m => m.FullName).Returns(target);
        _fileSystemMock
            .Setup(fs => fs.FileInfo.Wrap(It.IsAny<FileInfo>()))
            .Returns(targetFileInfoMock.Object);
        _fileSystemMock
            .Setup(fs => fs.DirectoryInfo.Wrap(It.IsAny<DirectoryInfo>()))
            .Returns(directoryInfoMock.Object);
        
        const string resolvedSolutionPath = "solution.sln";
        var resolvedSolutionMock = new Mock<IFileInfo>();
        resolvedSolutionMock.Setup(m => m.Exists).Returns(true);
        resolvedSolutionMock.Setup(m => m.FullName).Returns(resolvedSolutionPath);
        
        const string resolvedProjectPath = "project.csproj";
        var resolvedProjectMock = new Mock<IFileInfo>();
        resolvedProjectMock.Setup(m => m.Exists).Returns(true);
        resolvedProjectMock.Setup(m => m.FullName).Returns(resolvedProjectPath);

        const string resolvedAssemblyPath = "assembly.dll";
        var resolvedAssemblyMock = new Mock<IFileInfo>();
        resolvedAssemblyMock.Setup(m => m.Exists).Returns(true);
        resolvedAssemblyMock.Setup(m => m.FullName).Returns(resolvedAssemblyPath);
        
        _directoryStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (resolvedSolutionMock.Object, TargetType.Solution) });
        _solutionStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (resolvedProjectMock.Object, TargetType.Project) });
        _projectStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (resolvedAssemblyMock.Object, TargetType.Assembly) });
        _assemblyStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(IFileSystemInfo, TargetType)> { (resolvedAssemblyMock.Object, TargetType.Assembly) });
    
        // act
        var result = _targetResolver.Resolve(target);
    
        // assert
        Assert.Single(result);
        _directoryStrategy.Verify(s => s.Resolve(target), Times.Once);
        _solutionStrategy.Verify(s => s.Resolve(resolvedSolutionPath), Times.Once);
        _projectStrategy.Verify(s => s.Resolve(resolvedProjectPath), Times.Once);
        _assemblyStrategy.Verify(s => s.Resolve(resolvedAssemblyPath), Times.Once);
    }
}

