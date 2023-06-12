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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

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
        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(It.IsAny<string>())).Returns((null, new Exception()));

        // act, assert
        Assert.Throws<FileNotFoundException>(() => _targetResolver.Resolve("").ToList());
    }

    [Fact]
    public void Resolve_ShouldThrowNotSupportedException_WhenUnsupportedTargetType()
    {
        // arrange
        const string target = "target.unknown";
        var targetFileSystemInfo = new FileInfo(target);
        
        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(target)).Returns((targetFileSystemInfo, default));
        _fileSystemMock.Setup(fs => fs.IsFile(targetFileSystemInfo)).Returns(true);

        // act, assert
        Assert.Throws<NotSupportedException>(() => _targetResolver.Resolve(target).ToList());
    }

    [Fact]
    public void Resolve_ShouldResolve_WhenTargetIsAssembly()
    {
        // arrange
        const string target = "target.dll";
        var targetFile = new FileInfo(target);

        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(target)).Returns((targetFile, default));
        _fileSystemMock.Setup(fs => fs.IsFile(It.IsAny<FileSystemInfo>())).Returns(true);
        _assemblyStrategy
            .Setup(s => s.Resolve(It.IsAny<string>()))
            .Returns(new List<(FileSystemInfo, TargetType)> { (new FileInfo(target), TargetType.Assembly) });

        // act
        var result = _targetResolver.Resolve(target);

        // assert
        Assert.Single(result);
        _assemblyStrategy.Verify(s => s.Resolve(targetFile.FullName), Times.Once);
    }

    [Fact]
    public void Resolve_ShouldResolve_WhenTargetIsProject()
    {
        // arrange
        const string target = "target.csproj";
        var targetFile = new FileInfo(target);
        var resolvedAssembly = new FileInfo("assembly.dll");

        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(target)).Returns((targetFile, default));
        _fileSystemMock.Setup(fs => fs.IsFile(It.IsAny<FileSystemInfo>())).Returns(true);
        _projectStrategy
            .Setup(s => s.Resolve(targetFile.FullName))
            .Returns(new List<(FileSystemInfo, TargetType)> { (resolvedAssembly, TargetType.Assembly) });
        _assemblyStrategy
            .Setup(s => s.Resolve(resolvedAssembly.FullName))
            .Returns(new List<(FileSystemInfo, TargetType)> { (resolvedAssembly, TargetType.Assembly) });

        // act
        var result = _targetResolver.Resolve(target);

        // assert
        Assert.Single(result);
        _projectStrategy.Verify(s => s.Resolve(targetFile.FullName), Times.Once);
        _assemblyStrategy.Verify(s => s.Resolve(resolvedAssembly.FullName), Times.Once);
    }

    [Fact]
    public void Resolve_ShouldResolve_WhenTargetIsSolution()
    {
        // arrange
        const string target = "target.sln";
        var targetFile = new FileInfo(target);
        const string project = "project.csproj";
        var resolvedProjectFile = new FileInfo(project);
        const string assembly = "assembly.dll";
        var resolvedAssemblyFile = new FileInfo(assembly);

        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(target)).Returns((targetFile, default));
        _fileSystemMock.Setup(fs => fs.IsFile(It.IsAny<FileSystemInfo>())).Returns(true);
        _solutionStrategy
            .Setup(s => s.Resolve(targetFile.FullName))
            .Returns(new List<(FileSystemInfo, TargetType)> { (resolvedProjectFile, TargetType.Project) });
        _projectStrategy
            .Setup(s => s.Resolve(resolvedProjectFile.FullName))
            .Returns(new List<(FileSystemInfo, TargetType)> { (resolvedAssemblyFile, TargetType.Assembly) });
        _assemblyStrategy
            .Setup(s => s.Resolve(resolvedAssemblyFile.FullName))
            .Returns(new List<(FileSystemInfo, TargetType)> { (resolvedAssemblyFile, TargetType.Assembly) });

        // act
        var result = _targetResolver.Resolve(target);

        // assert
        Assert.Single(result);
        _solutionStrategy.Verify(s => s.Resolve(targetFile.FullName), Times.Once);
        _projectStrategy.Verify(s => s.Resolve(resolvedProjectFile.FullName), Times.Once);
        _assemblyStrategy.Verify(s => s.Resolve(resolvedAssemblyFile.FullName), Times.Once);
    }

    [Fact]
    public void Resolve_ShouldResolve_WhenTargetIsDirectory()
    {
        // arrange
        const string target = "target";
        var targetDirectory = new DirectoryInfo(target);
        const string solution = "solution.sln";
        var resolvedSolutionFile = new FileInfo(solution);
        const string project = "project.csproj";
        var resolvedProjectFile = new FileInfo(project);
        const string assembly = "assembly.dll";
        var resolvedAssemblyFile = new FileInfo(assembly);

        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(target)).Returns((targetDirectory, default));
        _fileSystemMock.Setup(fs => fs.IsFile(targetDirectory)).Returns(false);
        _directoryStrategy
            .Setup(s => s.Resolve(targetDirectory.FullName))
            .Returns(new List<(FileSystemInfo, TargetType)> { (resolvedSolutionFile, TargetType.Solution) });
        _solutionStrategy
            .Setup(s => s.Resolve(resolvedSolutionFile.FullName))
            .Returns(new List<(FileSystemInfo, TargetType)> { (resolvedProjectFile, TargetType.Project) });
        _projectStrategy
            .Setup(s => s.Resolve(resolvedProjectFile.FullName))
            .Returns(new List<(FileSystemInfo, TargetType)> { (resolvedAssemblyFile, TargetType.Assembly) });
        _assemblyStrategy
            .Setup(s => s.Resolve(resolvedAssemblyFile.FullName))
            .Returns(new List<(FileSystemInfo, TargetType)> { (resolvedAssemblyFile, TargetType.Assembly) });

        // act
        var result = _targetResolver.Resolve(target);

        // assert
        Assert.Single(result);
        _directoryStrategy.Verify(s => s.Resolve(targetDirectory.FullName), Times.Once);
        _solutionStrategy.Verify(s => s.Resolve(resolvedSolutionFile.FullName), Times.Once);
        _projectStrategy.Verify(s => s.Resolve(resolvedProjectFile.FullName), Times.Once);
        _assemblyStrategy.Verify(s => s.Resolve(resolvedAssemblyFile.FullName), Times.Once);
    }
}

