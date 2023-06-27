using System.IO.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

internal class FileReader : IFileReader
{
    private readonly IFileSystem _fileSystem;

    public FileReader(IFileSystem fileSystem)
    {
        _fileSystem = fileSystem;
    }
    
    public async IAsyncEnumerable<(string, int)> ReadLinesAsync(string path)
    {
        var lineNumber = 0;
        await using var stream = _fileSystem.File.OpenRead(path);
        using var reader = new StreamReader(stream);
        while (await reader.ReadLineAsync() is {} line)
        {
            lineNumber++;
            yield return (line, lineNumber);
        }
    }
}