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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Suppress;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.App;

public class SuppressCommandHandlerTests
{
    private readonly Mock<ITargetResolver> _mockTargetResolver;
    private readonly Mock<ITestSelectorsLoader> _mockTestSelectorsFactory;
    private readonly Mock<IAssemblyPatcher> _mockAssemblyPatcher;
    private readonly Mock<IBackupMetadataSaver> _mockBackupMetadataSaver;
    private readonly SuppressCommandHandler _handler;
    
    private readonly SuppressCommand _testCommand = new();

    public SuppressCommandHandlerTests()
    {
        _mockTargetResolver = new Mock<ITargetResolver>();
        _mockTestSelectorsFactory = new Mock<ITestSelectorsLoader>();
        _mockAssemblyPatcher = new Mock<IAssemblyPatcher>();
        _mockBackupMetadataSaver = new Mock<IBackupMetadataSaver>();
        var mockLogger = new Mock<ILogger<SuppressCommandHandler>>();
        _handler = new SuppressCommandHandler(_mockTargetResolver.Object, _mockTestSelectorsFactory.Object,
            _mockAssemblyPatcher.Object, _mockBackupMetadataSaver.Object, mockLogger.Object);
    }
    
    [Fact]
    public async Task ExecuteAsync_ShouldPatchAssemblyAndSaveBackupMetadata_WhenPatchingIsSuccessful()
    {
        // arrange
        var targetAssembly1 = new FileInfo("TestAssembly1");
        var targetAssembly2 = new FileInfo("TestAssembly2");
        var testSelectors = new Dictionary<string, ITestSelector>();
        var assemblyMutationResult = new AssemblyMutationResult(0, 0);
        const string backupAssemblyPath1 = "BackupAssemblyPath1";
        const string backupAssemblyPath2 = "BackupAssemblyPath1";
        const string assemblySymbolsPath = "assemblySymbolsPath1";
        const string backupAssemblySymbolsPath = "BackupAssemblySymbolsPath1";
        var patchingResult1 =
            AssemblyPatchingResult.Patched(targetAssembly1.Name, backupAssemblyPath1, null, null, assemblyMutationResult);
        var patchingResult2 =
            AssemblyPatchingResult.Patched(targetAssembly2.Name, backupAssemblyPath2, assemblySymbolsPath, backupAssemblySymbolsPath, assemblyMutationResult);
        _mockTestSelectorsFactory
            .Setup(m => m.LoadTestSelectorsFromAsync(It.IsAny<string>()))
            .ReturnsAsync(testSelectors);
        _mockTargetResolver
            .Setup(m => m.Resolve(It.IsAny<string>()))
            .Returns(new[] { targetAssembly1, targetAssembly2 });
        _mockAssemblyPatcher
            .Setup(m => m.TryPatchAsync(targetAssembly1, It.IsAny<TestSuppressionPatchingCriteria>()))
            .ReturnsAsync(patchingResult1);
        _mockAssemblyPatcher
            .Setup(m => m.TryPatchAsync(targetAssembly2, It.IsAny<TestSuppressionPatchingCriteria>()))
            .ReturnsAsync(patchingResult2);
        
        // act
        await _handler.ExecuteAsync(_testCommand);
        
        // assert
        _mockTestSelectorsFactory.Verify(m =>
            m.LoadTestSelectorsFromAsync(It.Is<string>(s => s == _testCommand.TestsFilePath)),
            Times.Once
        );
        _mockAssemblyPatcher.Verify(m =>
            m.TryPatchAsync(targetAssembly1, It.Is<TestSuppressionPatchingCriteria>(c => c.InclusionMode == _testCommand.InclusionMode && c.TestSelectors == testSelectors)),
            Times.Once
        );
        _mockAssemblyPatcher.Verify(m =>
            m.TryPatchAsync(targetAssembly2, It.Is<TestSuppressionPatchingCriteria>(c => c.InclusionMode == _testCommand.InclusionMode && c.TestSelectors == testSelectors)),
            Times.Once
        );
        _mockBackupMetadataSaver.Verify(m =>
            m.SaveAsync(
                _testCommand.BackupFilePath,
                It.Is<BackupFileMetadata>(fm => fm.Path == targetAssembly1.Name && fm.BackupPath == backupAssemblyPath1)),
            Times.Once
        );
        _mockBackupMetadataSaver.Verify(m =>
                m.SaveAsync(
                    _testCommand.BackupFilePath,
                    It.Is<BackupFileMetadata>(fm => fm.Path == targetAssembly2.Name && fm.BackupPath == backupAssemblyPath2 )),
            Times.Once
        );
        _mockBackupMetadataSaver.Verify(m =>
                m.SaveAsync(
                    _testCommand.BackupFilePath,
                    It.Is<BackupFileMetadata>(fm => fm.Path == assemblySymbolsPath && fm.BackupPath == backupAssemblySymbolsPath )),
            Times.Once
        );
    }
    
    [Fact]
    public async Task ExecuteAsync_ShouldNotPatchAssemblyAndNotSaveBackupMetadata_WhenPatchingIsNotSuccessful()
    {
        
        // arrange
        var targetAssembly = new FileInfo("TestAssembly1");
        var testSelectors = new Dictionary<string, ITestSelector>();
        var patchingResult =
            AssemblyPatchingResult.NotPatched(targetAssembly.Name);
        _mockTestSelectorsFactory
            .Setup(m => m.LoadTestSelectorsFromAsync(It.IsAny<string>()))
            .ReturnsAsync(testSelectors);
        _mockTargetResolver
            .Setup(m => m.Resolve(It.IsAny<string>()))
            .Returns(new[] { targetAssembly });
        _mockAssemblyPatcher
            .Setup(m => m.TryPatchAsync(targetAssembly, It.IsAny<TestSuppressionPatchingCriteria>()))
            .ReturnsAsync(patchingResult);
        
        // act
        await _handler.ExecuteAsync(_testCommand);
        
        // assert
        _mockTestSelectorsFactory.Verify(m =>
            m.LoadTestSelectorsFromAsync(It.Is<string>(s => s == _testCommand.TestsFilePath)),
            Times.Once
        );
        _mockAssemblyPatcher.Verify(m =>
            m.TryPatchAsync(targetAssembly, It.Is<TestSuppressionPatchingCriteria>(c => c.InclusionMode == _testCommand.InclusionMode && c.TestSelectors == testSelectors)),
            Times.Once
        );
        _mockBackupMetadataSaver.Verify(m =>
            m.SaveAsync(It.IsAny<string>(), It.IsAny<BackupFileMetadata>()),
            Times.Never
        );
    }
}


