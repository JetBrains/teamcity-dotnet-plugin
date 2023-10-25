using Microsoft.Build.Locator;
using Microsoft.Extensions.Logging;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.MsBuild;

public class MsBuildLocatorWrapper : IMsBuildLocator
{
    private readonly ILogger<MsBuildLocatorWrapper> _logger;

    public MsBuildLocatorWrapper(ILogger<MsBuildLocatorWrapper> logger)
    {
        _logger = logger;
    }
    
    public void RegisterDefaultMsBuild()
    {
        var instance = MSBuildLocator.RegisterDefaults();
        _logger.LogDebug(
            "Target project resolver uses MSBuild from {InstallationName} {InstallationVersion} located at the path {InstallationPath}",
            instance.Name,
            instance.Version,
            instance.MSBuildPath
        );
    }
}