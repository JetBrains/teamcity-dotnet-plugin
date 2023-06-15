using System.IO.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

internal static class FileSystemExtensions
{
    public static bool IsFile(this IFileSystemInfo fileSystemInfo) => fileSystemInfo is IFileInfo;

    public static (IFileSystemInfo?, Exception?) GetFileSystemInfo(this IFileSystem fileSystem, string path)
    {
        try
        {
            var fileInfo = fileSystem.FileInfo.Wrap(new FileInfo(path));
            var directoryInfo = fileSystem.DirectoryInfo.Wrap(new DirectoryInfo(path));

            if (fileInfo.Exists)
            {
                return (fileInfo, null);
            }
            
            if (directoryInfo.Exists)
            {
                return (directoryInfo, null);
            }
                
            return (null, new Exception($"Path not found: {path}"));
        } 
        catch (Exception exception)
        {
            return (null, new Exception($"Can't get file system info for the path {path}", exception));
        }
    }

    public static (IFileInfo?, Exception?) GetFileInfo(this IFileSystem fileSystem, string path)
    {
        if (!fileSystem.File.Exists(path))
        {
            return (null, new Exception($"File not found: {path}"));
        }
        
        try
        {
            return (fileSystem.FileInfo.New(path), null);
        }
        catch (Exception exception)
        {
            return (null, new Exception($"Can't get file info for {path}", exception));
        }
    }

    public static (IDirectoryInfo?, Exception?) GetDirectoryInfo(this IFileSystem fileSystem, string path)
    {
        if (!fileSystem.Directory.Exists(path))
        {
            return (null, new Exception($"Directory not found: {path}"));
        }
        
        try
        {
            return (fileSystem.DirectoryInfo.New(path), null);
        }
        catch (Exception exception)
        {
            return (null, new Exception($"Can't get directory info for {path}", exception));
        }
    }

    public static async Task CopyFile(this IFileSystem fileSystem, string source, string target)
    {
        await using var sourceStream = fileSystem.File.OpenRead(source);
        await using var destinationStream = fileSystem.File.Create(target);
        await sourceStream.CopyToAsync(destinationStream);
    }

    public static bool IsDirectory(this IFileSystemInfo fileSystemInfo) => fileSystemInfo is IDirectoryInfo;
}