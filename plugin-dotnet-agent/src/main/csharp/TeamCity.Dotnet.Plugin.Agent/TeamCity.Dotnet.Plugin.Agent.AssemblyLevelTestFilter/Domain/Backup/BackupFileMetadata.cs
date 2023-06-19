using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Backup;

internal record BackupFileMetadata(string OriginalPath, string BackupPath)
{
    private const string Separator = ";";

    public override string ToString() => $"\"{BackupPath}\"{Separator}\"{OriginalPath}\"";

    public static Result<BackupFileMetadata, string> FromString(string metadataEntry)
    {
        var paths = metadataEntry.Split(Separator).Select(s => s.Trim('"')).ToArray();
        if (paths.Length != 2)
        {
            return Result<BackupFileMetadata, string>.Error(
                $"Invalid metadata entry`{metadataEntry}`: it should contain two quoted paths separated by `{Separator}`");
        }
        
        return Result<BackupFileMetadata, string>.Success(new BackupFileMetadata(paths[1], paths[0]));
    }
}

