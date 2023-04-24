using YamlDotNet.Serialization;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;

internal class YamlBackupMetadataSaver : IBackupMetadataSaver
{
    private readonly string _yamlFilePath;

    public YamlBackupMetadataSaver(string yamlFilePath)
    {
        _yamlFilePath = yamlFilePath;
    }

    public async Task SaveAsync(BackupAssemblyMetadata backupMetadata)
    {
        await using var streamWriter = new StreamWriter(_yamlFilePath, append: true);
        var serializer = new SerializerBuilder().Build();
        var yamlContent = serializer.Serialize(backupMetadata);
        await streamWriter.WriteAsync(yamlContent);
        await streamWriter.FlushAsync();
    }
}