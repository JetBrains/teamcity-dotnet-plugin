using Microsoft.Build.Locator;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.MsBuild;

public interface IMsBuildLocator
{
    VisualStudioInstance RegisterDefaults();
}