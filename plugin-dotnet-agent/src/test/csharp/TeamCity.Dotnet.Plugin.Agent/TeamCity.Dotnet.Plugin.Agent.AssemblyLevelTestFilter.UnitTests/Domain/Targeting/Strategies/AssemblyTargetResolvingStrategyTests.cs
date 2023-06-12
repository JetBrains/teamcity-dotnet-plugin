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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.Targeting.Strategies;

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
        const string target = "invalid";

        _fileSystemMock.Setup(fs => fs.FileExists(target)).Returns(false);

        var result = _strategy.Resolve(target);

        Assert.Empty(result);
    }

    [Fact]
    public void Resolve_ShouldYieldBreak_WhenUnsupportedExtension()
    {
        const string target = "target.unsupported";

        _fileSystemMock.Setup(fs => fs.FileExists(target)).Returns(true);
        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(target)).Returns((new FileInfo(target), default));

        var result = _strategy.Resolve(target);

        Assert.Empty(result);
    }

    [Fact]
    public void Resolve_ShouldYieldBreak_WhenNotNetAssembly()
    {
        const string target = "target.dll";

        _fileSystemMock.Setup(fs => fs.FileExists(target)).Returns(true);
        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(target)).Returns((new FileInfo(target), default));
        _assemblyLoaderMock.Setup(a => a.LoadAssembly(target, false)).Returns(default(IDotnetAssembly));

        var result = _strategy.Resolve(target);

        Assert.Empty(result);
    }

    [Fact]
    public void Resolve_ShouldYieldBreak_WhenNoSupportedTestFrameworks()
    {
        const string target = "target.dll";

        _fileSystemMock.Setup(fs => fs.FileExists(target)).Returns(true);
        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(target)).Returns((new FileInfo(target), default));
        var assemblyMock = new Mock<IDotnetAssembly>();
        _assemblyLoaderMock.Setup(a => a.LoadAssembly(target, false)).Returns(assemblyMock.Object);

        var result = _strategy.Resolve(target);

        Assert.Empty(result);
    }

    [Fact]
    public void Resolve_ShouldReturnResolvedAssembly_WhenSupportedTestFrameworks()
    {
        const string target = "target.dll";
        var targetFileSystemInfo = new FileInfo(target);
        
        _fileSystemMock.Setup(fs => fs.FileExists(target)).Returns(true);
        _fileSystemMock.Setup(fs => fs.GetFileSystemInfo(target)).Returns((targetFileSystemInfo, default));
        
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

        var result = _strategy.Resolve(target).ToList();

        Assert.Single(result);
        Assert.Equal(targetFileSystemInfo.FullName, result[0].Item1.FullName);
        Assert.Equal(_strategy.TargetType, result[0].Item2);
    }
    
    internal class TestTestEngine1 : ITestEngine
    {
        public string Name => "TestEngine1";
        public IEnumerable<string> AssembliesNames { get; } = new[] { "assembly1", "assembly2" };
        public IReadOnlyList<string> TestClassAttributes => new List<string>();
        public IReadOnlyList<string> TestMethodAttributes => new List<string>();
    }
    
    internal class TestTestEngine2 : ITestEngine
    {
        public string Name => "TestEngine2";
        public IEnumerable<string> AssembliesNames { get; } = new[] { "assembly3", "assembly4" };
        public IReadOnlyList<string> TestClassAttributes => new List<string>();
        public IReadOnlyList<string> TestMethodAttributes => new List<string>();
    }
}