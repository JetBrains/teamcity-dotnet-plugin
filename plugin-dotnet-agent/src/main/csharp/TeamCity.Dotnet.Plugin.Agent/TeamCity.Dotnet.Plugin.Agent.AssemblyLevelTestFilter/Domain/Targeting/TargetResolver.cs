namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal class TargetResolver : ITargetResolver
{
    private readonly IDictionary<TargetType, ITargetResolvingStrategy> _strategies;

    public TargetResolver(IEnumerable<ITargetResolvingStrategy> strategies)
    {
        _strategies = strategies.ToDictionary(s => s.TargetType);
    }

    public IAsyncEnumerable<FileInfo> ResolveAsync(string target)
    {
        var fileInfo = new FileInfo(target);

        if (!fileInfo.Exists)
        {
            throw new FileNotFoundException($"Target '{target}' not found.");
        }

        var extension = fileInfo.Extension.ToLowerInvariant();
        var targetType = GetTargetType(extension, fileInfo);
        return _strategies[targetType].FindAssembliesAsync(target);
    }

    private static TargetType GetTargetType(string extension, FileSystemInfo fileInfo)
    {
        if (fileInfo.Attributes.HasFlag(FileAttributes.Directory))
        {
            return TargetType.Directory;
        }
        
        if (extension == TargetType.Assembly.FileExtension())
        {
            return TargetType.Assembly;
        }

        if (extension == TargetType.Project.FileExtension())
        {
            return TargetType.Project;
        }

        if (extension == TargetType.Solution.FileExtension())
        {
            return TargetType.Solution;
        }

        throw new NotSupportedException($"Unsupported target type: '{extension}'.");
    }
}

