using System.IO.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

internal static class FileSystemExtensions
{
    public static bool IsFile(this IFileSystemInfo fileSystemInfo) => fileSystemInfo is IFileInfo;

    public static Result<IFileSystemInfo, Exception> TryGetFileSystemInfo(this IFileSystem fileSystem, string path)
    {
        try
        {
            var fileInfo = fileSystem.FileInfo.Wrap(new FileInfo(path));
            var directoryInfo = fileSystem.DirectoryInfo.Wrap(new DirectoryInfo(path));

            if (fileInfo.Exists)
            {
                return Result<IFileSystemInfo, Exception>.Success(fileInfo);
            }
            
            if (directoryInfo.Exists)
            {
                return Result<IFileSystemInfo, Exception>.Success(directoryInfo);
            }
                
            return Result<IFileSystemInfo, Exception>.Error(new Exception($"Path not found: {path}"));
        } 
        catch (Exception exception)
        {
            return Result<IFileSystemInfo, Exception>.Error(new Exception($"Can't get file system info for the path {path}", exception));
        }
    }

    public static Result<IFileInfo, Exception> TryGetFileInfo(this IFileSystem fileSystem, string path)
    {
        if (!fileSystem.File.Exists(path))
        {
            return Result<IFileInfo, Exception>.Error(new Exception($"File not found: {path}"));
        }
        
        try
        {
            return Result<IFileInfo, Exception>.Success(fileSystem.FileInfo.New(path));
        }
        catch (Exception exception)
        {
            return Result<IFileInfo, Exception>.Error(new Exception($"Can't get file info for {path}", exception));
        }
    }

    public static Result<IDirectoryInfo, Exception> TryGetDirectoryInfo(this IFileSystem fileSystem, string path)
    {
        if (!fileSystem.Directory.Exists(path))
        {
            return Result<IDirectoryInfo, Exception>.Error(new Exception($"Directory not found: {path}"));
        }
        
        try
        {
            return Result<IDirectoryInfo, Exception>.Success(fileSystem.DirectoryInfo.New(path));
        }
        catch (Exception exception)
        {
            return Result<IDirectoryInfo, Exception>.Error(new Exception($"Can't get directory info for {path}", exception));
        }
    }

    public static bool IsDirectory(this IFileSystemInfo fileSystemInfo) => fileSystemInfo is IDirectoryInfo;
}