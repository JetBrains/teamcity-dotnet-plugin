namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

public interface IFileReader
{
    IAsyncEnumerable<(string, int)> ReadLinesAsync(string path);
}