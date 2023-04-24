namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal interface ITargetResolver
{
    IAsyncEnumerable<FileInfo> ResolveAsync(string target);
}