using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.Backup;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.Backup;

public class BackupRestoreTests
{
    private readonly Mock<FileSystem> _fileSystemMock;
    private readonly Mock<IFileReader> _fileReaderMock;
    private readonly Mock<ILogger<BackupRestore>> _loggerMock;
    private readonly BackupRestore _backupRestore;

    public BackupRestoreTests()
    {
        _fileSystemMock = new Mock<FileSystem>();
        _fileReaderMock = new Mock<IFileReader>();
        _loggerMock = new Mock<ILogger<BackupRestore>>();
        _backupRestore = new BackupRestore(_fileSystemMock.Object, _fileReaderMock.Object, _loggerMock.Object);
    }

    [Fact]
    public async Task RestoreAsync_MetadataFileDoesNotExist_ReturnsImmediately()
    {
        // arrange
        const string csvFilePath = "path_to_csv";

        var fileMock = new Mock<IFile>();
        fileMock.Setup(m => m.Exists(csvFilePath)).Returns(false);
        _fileSystemMock.Setup(fs => fs.File).Returns(fileMock.Object);

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.File.OpenRead(It.IsAny<string>()), Times.Never);
    }

    [Fact]
    public async Task RestoreAsync_BackupFileDoesNotExist_DoesNotAttemptRestore()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        var fileMock = new Mock<IFile>();
        fileMock.Setup(m => m.Exists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File).Returns(fileMock.Object);
        _fileReaderMock
            .Setup(m => m.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));
        var backupFileMock = new Mock<IFile>();
        backupFileMock.Setup(m => m.Exists(backupFilePath)).Returns(false);
        _fileSystemMock.Setup(fs => fs.File).Returns(backupFileMock.Object);

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.File.Delete(originalFilePath), Times.Never);
        _fileSystemMock.Verify(fs => fs.File.Move(backupFilePath, originalFilePath), Times.Never);
    }

    [Fact]
    public async Task RestoreAsync_ValidBackupAndOriginalFiles_AttemptsRestoreAndDeletesMetadata()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.File.Exists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(originalFilePath)).Returns(true);
        _fileReaderMock
            .Setup(m => m.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.File.Delete(originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.File.Move(backupFilePath, originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.File.Delete(csvFilePath), Times.Once);
    }
        
        [Fact]
    public async Task RestoreAsync_InvalidLine_DoesNotAttemptRestore()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        var invalidLine = "\"invalid_line\"";

        _fileSystemMock.Setup(fs => fs.File.Exists(csvFilePath)).Returns(true);
        _fileReaderMock
            .Setup(m => m.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(new[] {(invalidLine, 1)}));

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.File.Delete(It.IsAny<string>()), Times.Once);
        _fileSystemMock.Verify(fs => fs.File.Move(It.IsAny<string>(), It.IsAny<string>()), Times.Never);
    }

    [Fact]
    public async Task RestoreAsync_OriginalFileDoesNotExist_StillAttemptsRestore()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.File.Exists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(originalFilePath)).Returns(false);
        _fileReaderMock.Setup(m => m.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.File.Delete(originalFilePath), Times.Never);
        _fileSystemMock.Verify(fs => fs.File.Move(backupFilePath, originalFilePath), Times.Once);
    }

    [Fact]
    public async Task RestoreAsync_OriginalFileDeleteThrowsException_StillAttemptsRestore()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.File.Exists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(originalFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Delete(originalFilePath)).Throws<Exception>();
        _fileReaderMock
            .Setup(m => m.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.File.Delete(originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.File.Move(backupFilePath, originalFilePath), Times.Once);
    }

    [Fact]
    public async Task RestoreAsync_FileMoveThrowsException_LogsError()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.File.Exists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(originalFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Move(backupFilePath, originalFilePath)).Throws<Exception>();
        _fileReaderMock
            .Setup(m => m.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.File.Delete(originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.File.Move(backupFilePath, originalFilePath), Times.Once);
    }

    [Fact]
    public async Task RestoreAsync_MetadataFileDeleteThrowsException_LogsError()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.File.Exists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Exists(originalFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.File.Delete(csvFilePath)).Throws<Exception>();
        _fileReaderMock
            .Setup(m => m.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.File.Delete(originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.File.Move(backupFilePath, originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.File.Delete(csvFilePath), Times.Once);
    }



    private static IEnumerable<(string, int)> GetLines(string backupFilePath, string originalFilePath)
    {
        var line = $"\"{backupFilePath}\";\"{originalFilePath}\"";
        yield return (line, 1);
    }
}

public class TestAsyncEnumerable<T> : IAsyncEnumerable<T>
{
    private readonly IEnumerable<T> _enumerable;

    public TestAsyncEnumerable(IEnumerable<T> enumerable)
    {
        _enumerable = enumerable;
    }

    public IAsyncEnumerator<T> GetAsyncEnumerator(CancellationToken cancellationToken = new())
    {
        return new TestAsyncEnumerator<T>(_enumerable.GetEnumerator());
    }
}

public class TestAsyncEnumerator<T> : IAsyncEnumerator<T>
{
    private readonly IEnumerator<T> _enumerator;

    public TestAsyncEnumerator(IEnumerator<T> enumerator)
    {
        _enumerator = enumerator;
    }

    public ValueTask DisposeAsync()
    {
        _enumerator.Dispose();
        return ValueTask.CompletedTask;
    }

    public ValueTask<bool> MoveNextAsync()
    {
        var moved = _enumerator.MoveNext();
        return new ValueTask<bool>(moved);
    }

    public T Current => _enumerator.Current;
}
