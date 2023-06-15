using Microsoft.Build.Locator;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.MsBuild;

public interface IMsBuildLocator
{
    VisualStudioInstance RegisterDefaults();
}