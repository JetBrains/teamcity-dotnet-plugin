
namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.FileSystemExtensions;

public interface IFileCopier
{
    Task CopyFile(string source, string target);
}