using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Extensions;

public static class EnumExtensions
{
    public static string GetDockerTag(this Enum value)
    {
        var field = value.GetType().GetField(value.ToString());
        var attribute = field?.GetCustomAttributes(typeof(DockerTagAttribute), false) as DockerTagAttribute[];

        return attribute?.Length > 0 ? attribute[0].Tag : value.ToString();
    }
    
    public static string GetMoniker(this Enum value)
    {
        var field = value.GetType().GetField(value.ToString());
        var attribute = field?.GetCustomAttributes(typeof(MonikerAttribute), false) as MonikerAttribute[];

        return attribute?.Length > 0 ? attribute[0].Moniker : value.ToString();
    }
}