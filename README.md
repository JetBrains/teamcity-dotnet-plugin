## .NET CLI Plugin for [<img src="https://cdn.worldvectorlogo.com/logos/teamcity.svg" height="20" align="center"/>](https://www.jetbrains.com/teamcity/)

[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub) <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityDotnetCorePluginBuild&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityDotnetCorePluginBuild)/statusIcon.svg" alt=""/></a>

The TeamCity .NET plugin provides support for [.NET CLI](https://www.microsoft.com/net/download/core) in TeamCity.

It simplifies building Windows and cross-platform applications that use .NET frameworks and libraries.

# Features

The plugin provides the following features for .NET project building:
* `.NET CLI (dotnet)` build runner
* .NET CLI & .NET Core SDK detection on TeamCity build agents
* Auto-discovery of build steps
* On-the-fly test reporting
* Supports code coverage tools
* Cleanup of NuGet caches to meet the agent [free space requirements](https://confluence.jetbrains.com/display/TCDL/Free+disk+space)
 
# Download

You can [download the plugin](https://plugins.jetbrains.com/plugin/9190?pr=teamcity) and install it as [an additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

# Compatibility

The current version of plugin is compatible with [TeamCity 2017.1+](https://www.jetbrains.com/teamcity/download/) and [.NET CLI 1.0+](https://www.microsoft.com/net/download/core).

# Configuration

## .NET CLI toolkit

To use the `dotnet` build runner, install [.NET CLI](https://www.microsoft.com/net/core) and add the .NET CLI tools path to the `PATH` environment variable.

You can also configure the `DOTNET_HOME` environment variable for your TeamCity build agent user, for instance:

```
DOTNET_HOME=C:\Program Files\dotnet\
```

# Build

This project uses gradle as a build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or write an issue.
