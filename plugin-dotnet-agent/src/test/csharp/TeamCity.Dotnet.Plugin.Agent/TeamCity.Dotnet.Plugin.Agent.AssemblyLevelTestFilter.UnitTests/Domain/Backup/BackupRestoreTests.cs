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

public class BackupRestoreTests
{
    private readonly Mock<IFileSystem> _fileSystemMock;
    private readonly Mock<ILogger<BackupRestore>> _loggerMock;
    private readonly BackupRestore _backupRestore;

    public BackupRestoreTests()
    {
        _fileSystemMock = new Mock<IFileSystem>();
        _loggerMock = new Mock<ILogger<BackupRestore>>();
        _backupRestore = new BackupRestore(_fileSystemMock.Object, _loggerMock.Object);
    }

    [Fact]
    public async Task RestoreAsync_MetadataFileDoesNotExist_ReturnsImmediately()
    {
        // arrange
        const string csvFilePath = "path_to_csv";

        _fileSystemMock.Setup(fs => fs.FileExists(csvFilePath)).Returns(false);

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.ReadLinesAsync(It.IsAny<string>()), Times.Never);
    }

    [Fact]
    public async Task RestoreAsync_BackupFileDoesNotExist_DoesNotAttemptRestore()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.FileExists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));
        _fileSystemMock.Setup(fs => fs.FileExists(backupFilePath)).Returns(false);

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.DeleteFile(originalFilePath), Times.Never);
        _fileSystemMock.Verify(fs => fs.MoveFile(backupFilePath, originalFilePath), Times.Never);
    }

    [Fact]
    public async Task RestoreAsync_ValidBackupAndOriginalFiles_AttemptsRestoreAndDeletesMetadata()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.FileExists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));
        _fileSystemMock.Setup(fs => fs.FileExists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.FileExists(originalFilePath)).Returns(true);

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.DeleteFile(originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.MoveFile(backupFilePath, originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.DeleteFile(csvFilePath), Times.Once);
    }
        
        [Fact]
    public async Task RestoreAsync_InvalidLine_DoesNotAttemptRestore()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        var invalidLine = "\"invalid_line\"";

        _fileSystemMock.Setup(fs => fs.FileExists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(new[] {(invalidLine, 1)}));

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.DeleteFile(It.IsAny<string>()), Times.Once);
        _fileSystemMock.Verify(fs => fs.MoveFile(It.IsAny<string>(), It.IsAny<string>()), Times.Never);
    }

    [Fact]
    public async Task RestoreAsync_OriginalFileDoesNotExist_StillAttemptsRestore()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.FileExists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));
        _fileSystemMock.Setup(fs => fs.FileExists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.FileExists(originalFilePath)).Returns(false);

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.DeleteFile(originalFilePath), Times.Never);
        _fileSystemMock.Verify(fs => fs.MoveFile(backupFilePath, originalFilePath), Times.Once);
    }

    [Fact]
    public async Task RestoreAsync_OriginalFileDeleteThrowsException_StillAttemptsRestore()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.FileExists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));
        _fileSystemMock.Setup(fs => fs.FileExists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.FileExists(originalFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.DeleteFile(originalFilePath)).Throws<Exception>();

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.DeleteFile(originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.MoveFile(backupFilePath, originalFilePath), Times.Once);
    }

    [Fact]
    public async Task RestoreAsync_FileMoveThrowsException_LogsError()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.FileExists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));
        _fileSystemMock.Setup(fs => fs.FileExists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.FileExists(originalFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.MoveFile(backupFilePath, originalFilePath)).Throws<Exception>();

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.DeleteFile(originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.MoveFile(backupFilePath, originalFilePath), Times.Once);
    }

    [Fact]
    public async Task RestoreAsync_MetadataFileDeleteThrowsException_LogsError()
    {
        // arrange
        const string csvFilePath = "path_to_csv";
        const string backupFilePath = "path_to_backup";
        const string originalFilePath = "path_to_original";

        _fileSystemMock.Setup(fs => fs.FileExists(csvFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.ReadLinesAsync(csvFilePath))
            .Returns(new TestAsyncEnumerable<(string, int)>(GetLines(backupFilePath, originalFilePath)));
        _fileSystemMock.Setup(fs => fs.FileExists(backupFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.FileExists(originalFilePath)).Returns(true);
        _fileSystemMock.Setup(fs => fs.DeleteFile(csvFilePath)).Throws<Exception>();

        // act
        await _backupRestore.RestoreAsync(csvFilePath);

        // assert
        _fileSystemMock.Verify(fs => fs.DeleteFile(originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.MoveFile(backupFilePath, originalFilePath), Times.Once);
        _fileSystemMock.Verify(fs => fs.DeleteFile(csvFilePath), Times.Once);
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
