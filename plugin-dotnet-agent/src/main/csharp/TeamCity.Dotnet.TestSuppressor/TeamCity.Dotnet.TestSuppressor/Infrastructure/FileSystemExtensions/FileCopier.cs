using System.IO.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

internal class FileCopier : IFileCopier
{
    private readonly IFileSystem _fileSystem;

    public FileCopier(IFileSystem fileSystem)
    {
        _fileSystem = fileSystem;
    }
    
    public async Task CopyFile(string source, string target)
    {
        await using var sourceStream = _fileSystem.File.OpenRead(source);
        await using var destinationStream = _fileSystem.File.Create(target);
        await sourceStream.CopyToAsync(destinationStream);
    }
}