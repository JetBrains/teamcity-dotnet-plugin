namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal enum TargetType
{
    Directory,
    Solution,
    Project,
    Assembly
}

internal static class TargetTypeExtensions
{
    public static IEnumerable<string> GetPossibleFileExtension(this TargetType targetType)
    {
        switch (targetType)
        {
            case TargetType.Solution:
                yield return Infrastructure.FS.FileExtension.Solution;
                break;
            case TargetType.Project:
                yield return Infrastructure.FS.FileExtension.CSharpProject;
                yield return Infrastructure.FS.FileExtension.VisualBasicProject;
                yield return Infrastructure.FS.FileExtension.FSharpProject;
                yield return Infrastructure.FS.FileExtension.MsBuildProject;
                yield return Infrastructure.FS.FileExtension.MsBuildProject2;
                break;
            case TargetType.Assembly:
                yield return Infrastructure.FS.FileExtension.Dll;
                yield return Infrastructure.FS.FileExtension.Exe;
                break;
            case TargetType.Directory:
                throw new ArgumentOutOfRangeException(nameof(targetType), targetType, "Directory has no file extension");
            default:
                throw new ArgumentOutOfRangeException(nameof(targetType), targetType, "Unknown target type value to get file extension");
        };
    }
}