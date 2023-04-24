namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class AssemblyTargetResolvingStrategy : ITargetResolvingStrategy
{
    public TargetType TargetType => TargetType.Assembly;
    
    public async IAsyncEnumerable<FileInfo> FindAssembliesAsync(string target)
    {
        var fileInfo = new FileInfo(target);
        if (fileInfo.Exists)
        {
            yield return await Task.FromResult(fileInfo);
        }
    }
}