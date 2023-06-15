namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

public interface IFileReader
{
    IAsyncEnumerable<(string, int)> ReadLinesAsync(string path);
}