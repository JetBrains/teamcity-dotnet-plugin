namespace TeamCity.Dotnet.TestSuppressor.UnitTests;

internal static class StringExtensions
{
    private static class PathSeparator
    {
        public const char Windows = '\\';
        public const char Unix = '/';
    }

    private static class WindowsDrive
    {
        public const char C = 'C';
    }

    /// <summary>
    /// Handles string as path and normalize it in OS agnostic way
    /// </summary>
    public static string ToPlatformPath(this string path, char drive = WindowsDrive.C)
    {
        if (Path.DirectorySeparatorChar == PathSeparator.Unix)
        {
            return path.Replace(PathSeparator.Windows, PathSeparator.Unix);
        }
        
        var windowsPath = path.Replace(PathSeparator.Unix, PathSeparator.Windows);
        return path.StartsWith(PathSeparator.Unix.ToString()) ? $"{drive}:{windowsPath}" : windowsPath;
    }
}
