using System.Diagnostics.CodeAnalysis;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;

[SuppressMessage("ReSharper", "InconsistentNaming")]
public enum DotnetVersion
{
    [DockerTag("8.0-preview")]
    [Moniker("net8.0")]
    v8_0_Preview,
    
    [DockerTag("7.0")]
    [Moniker("net7.0")]
    v7_0,
    
    [DockerTag("6.0")]
    [Moniker("net6.0")]
    v6_0,
}

[AttributeUsage(AttributeTargets.Field)]
public class DockerTagAttribute : Attribute
{
    public DockerTagAttribute(string tag)
    {
        Tag = tag;
    }

    public string Tag { get; }
}

[AttributeUsage(AttributeTargets.Field)]
public class MonikerAttribute : Attribute
{
    public MonikerAttribute(string moniker)
    {
        Moniker = moniker;
    }

    public string Moniker { get; }
}
