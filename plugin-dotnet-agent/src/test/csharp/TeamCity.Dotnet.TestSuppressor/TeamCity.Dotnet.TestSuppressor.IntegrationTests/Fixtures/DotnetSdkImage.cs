using DotNet.Testcontainers.Builders;
using DotNet.Testcontainers.Images;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Extensions;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;

public class DotnetSdkImage : IImage
{
    private const string DockerfileName = "dotnet-sdk.dockerfile";
    private readonly DotnetVersion _dotnetVersion;
    private readonly IImage _image;
    
    private static string CurrentDirectory => Directory.GetCurrentDirectory();

    public DotnetSdkImage(DotnetVersion dotnetVersion)
    {
        _dotnetVersion = dotnetVersion;
        _image = new DockerImage("localhost/teamcity/dotnet-test-suppressor/dotnet", "sdk", _dotnetVersion.GetDockerTag());
        BuildImageAsync().ConfigureAwait(false).GetAwaiter().GetResult();
    }

    private async Task BuildImageAsync()
    {
        await new ImageFromDockerfileBuilder()
            .WithDockerfileDirectory(CurrentDirectory)
            .WithDockerfile(DockerfileName)
            .WithName(_image)
            .WithBuildArgument("DOTNET_SDK_IMAGE_TAG", _dotnetVersion.GetDockerTag())
            .WithDeleteIfExists(true)
            .WithCleanUp(true)
            .Build()
            .CreateAsync()
            .ConfigureAwait(false);
    }

    public string GetHostname() => _image.GetHostname();

    public string Repository => _image.Repository;

    public string Name => _image.Name;

    public string Tag => _image.Tag;

    public string FullName => _image.FullName;
}