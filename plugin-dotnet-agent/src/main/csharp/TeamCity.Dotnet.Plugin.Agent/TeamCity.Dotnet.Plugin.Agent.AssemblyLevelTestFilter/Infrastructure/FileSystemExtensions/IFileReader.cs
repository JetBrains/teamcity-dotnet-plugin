namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FileSystemExtensions;

public interface IFileReader
{
    IAsyncEnumerable<(string, int)> ReadLinesAsync(string path);
}