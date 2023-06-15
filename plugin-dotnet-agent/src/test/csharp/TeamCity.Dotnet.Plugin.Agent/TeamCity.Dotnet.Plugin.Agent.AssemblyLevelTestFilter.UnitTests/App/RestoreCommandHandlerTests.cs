using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.App.Restore;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;

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
        var pathMock = new Mock<IPath>();
        _fileSystemMock.Setup(m => m.Path).Returns(pathMock.Object);
        pathMock.Setup(m => m.GetFullPath(command.BackupMetadataFilePath)).Returns(fullPath);

        // act
        await _handler.ExecuteAsync(command);

        // assert
        _backupRestoreMock.Verify(br => br.RestoreAsync(fullPath), Times.Once);
    }
}
