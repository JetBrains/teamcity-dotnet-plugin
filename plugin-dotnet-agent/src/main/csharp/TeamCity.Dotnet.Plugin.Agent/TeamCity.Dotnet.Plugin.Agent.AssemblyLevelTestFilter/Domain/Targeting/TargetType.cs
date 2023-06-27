using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infxrastructure.FileSystemExtensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal enum TargetType
{
    MsBuildBinlog,
    Directory,
    Solution,
    Project,
    Assembly,
}

internal static class TargetTypeExtensions
{
    public static IEnumerable<string> GetPossibleFileExtension(this TargetType targetType)
    {
        switch (targetType)
        {
            case TargetType.Solution:
                yield return FileExtension.Solution;
                break;
            case TargetType.Project:
                yield return FileExtension.CSharpProject;
                yield return FileExtension.VisualBasicProject;
                yield return FileExtension.FSharpProject;
                yield return FileExtension.MsBuildProject;
                yield return FileExtension.MsBuildProject2;
                break;
            case TargetType.Assembly:
                yield return FileExtension.Dll;
                yield return FileExtension.Exe;
                break;
            case TargetType.MsBuildBinlog:
                yield return FileExtension.MsBuildBinaryLog;
                break;
            case TargetType.Directory:
                throw new ArgumentOutOfRangeException(nameof(targetType), targetType, "Directory has no file extension");
            default:
                throw new ArgumentOutOfRangeException(nameof(targetType), targetType, "Unknown target type value to get file extension");
        };
    }
}