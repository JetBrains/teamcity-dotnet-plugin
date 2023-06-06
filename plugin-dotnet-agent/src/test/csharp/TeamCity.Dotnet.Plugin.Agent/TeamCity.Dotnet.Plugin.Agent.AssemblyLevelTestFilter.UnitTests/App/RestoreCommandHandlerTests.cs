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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Restore;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.App;

public class RestoreCommandHandlerTests
{
    private readonly Mock<IBackupRestore> _backupRestoreMock;
    private readonly Mock<IFileSystem> _fileSystemMock;
    private readonly Mock<ILogger<RestoreCommandHandler>> _loggerMock;

    private readonly RestoreCommandHandler _handler;

    public RestoreCommandHandlerTests()
    {
        _backupRestoreMock = new Mock<IBackupRestore>();
        _fileSystemMock = new Mock<IFileSystem>();
        _loggerMock = new Mock<ILogger<RestoreCommandHandler>>();

        _handler = new RestoreCommandHandler(_backupRestoreMock.Object, _fileSystemMock.Object, _loggerMock.Object);
    }

    [Fact]
    public async Task ExecuteAsync_CallsBackupRestoreWithCorrectFilePath()
    {
        // arrange
        var command = new RestoreCommand
        {
            BackupMetadataFilePath = "path_to_metadata"
        };

        var fullPath = "full_path_to_metadata";
        _fileSystemMock.Setup(fs => fs.GetFullPath(command.BackupMetadataFilePath)).Returns(fullPath);

        // act
        await _handler.ExecuteAsync(command);

        // assert
        _backupRestoreMock.Verify(br => br.RestoreAsync(fullPath), Times.Once);
    }
}
