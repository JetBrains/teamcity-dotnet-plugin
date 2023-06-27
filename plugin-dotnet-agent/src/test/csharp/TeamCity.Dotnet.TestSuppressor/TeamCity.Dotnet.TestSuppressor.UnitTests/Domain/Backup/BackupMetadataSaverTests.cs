using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.Backup;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.Backup;

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
        var expectedText = $"\"{backupMetadata.BackupPath}\";\"{backupMetadata.OriginalPath}\"";
        var path = new Mock<IPath>();
        path.Setup(p => p.GetFullPath(filePath)).Returns(fullPath);
        _fileSystemMock.Setup(fs => fs.Path).Returns(path.Object);
        var fileMock = new Mock<IFile>();
        _fileSystemMock.Setup(m => m.File).Returns(fileMock.Object);

        // Act
        await _saver.SaveAsync(filePath, backupMetadata);

        // Assert
        _fileSystemMock.Verify(fs =>
            fs.File.AppendAllLinesAsync(
                fullPath,
                It.Is<IEnumerable<string>>(e => e.Contains(expectedText)),
                It.IsAny<CancellationToken>()
            ),
            Times.Once);
    }
}
