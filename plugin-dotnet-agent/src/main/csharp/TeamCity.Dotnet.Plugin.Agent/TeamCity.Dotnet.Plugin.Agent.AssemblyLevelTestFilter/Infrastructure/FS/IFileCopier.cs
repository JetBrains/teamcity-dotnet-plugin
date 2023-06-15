
namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

public interface IFileCopier
{
    Task CopyFile(string source, string target);
}