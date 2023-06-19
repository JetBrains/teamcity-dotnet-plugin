
namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FileSystemExtensions;

public interface IFileCopier
{
    Task CopyFile(string source, string target);
}