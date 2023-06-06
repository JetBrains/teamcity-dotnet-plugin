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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.Backup;

public class BackupMetadataSaverTests
{
    private readonly Mock<IFileSystem> _fileSystemMock;
    private readonly Mock<ILogger<BackupMetadataSaver>> _loggerMock;

    private readonly BackupMetadataSaver _saver;

    public BackupMetadataSaverTests()
    {
        _fileSystemMock = new Mock<IFileSystem>();
        _loggerMock = new Mock<ILogger<BackupMetadataSaver>>();

        _saver = new BackupMetadataSaver(_fileSystemMock.Object, _loggerMock.Object);
    }

    [Fact]
    public async Task SaveAsync_CallsFileSystemWithCorrectArguments()
    {
        // Arrange
        const string filePath = "path_to_file";
        const string fullPath = "full_path_to_file";
        var backupMetadata = new BackupFileMetadata("backup_path", "original_path");
        var expectedText = $"\"{backupMetadata.BackupPath}\";\"{backupMetadata.Path}\"";
        _fileSystemMock.Setup(fs => fs.GetFullPath(filePath)).Returns(fullPath);

        // Act
        await _saver.SaveAsync(filePath, backupMetadata);

        // Assert
        _fileSystemMock.Verify(fs => fs.AppendTextInFileAsync(fullPath, expectedText), Times.Once);
    }
}
