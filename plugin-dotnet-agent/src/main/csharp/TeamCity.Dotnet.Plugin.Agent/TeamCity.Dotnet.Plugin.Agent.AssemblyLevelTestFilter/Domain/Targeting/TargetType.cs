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
    public static string FileExtension(this TargetType targetType)
    {
        return targetType switch
        {
            TargetType.Directory => "",
            TargetType.Solution => ".sln",
            TargetType.Project => ".csproj",
            TargetType.Assembly => ".dll",
            _ => throw new ArgumentOutOfRangeException(nameof(targetType), targetType, "Unknown TargetType value."),
        };
    }
}