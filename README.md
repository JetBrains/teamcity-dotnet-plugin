## .NET CLI Plugin for [<img src="https://cdn.worldvectorlogo.com/logos/teamcity.svg" height="20" align="center"/>](https://www.jetbrains.com/teamcity/)

[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub) <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityDotnetCorePluginBuild&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityDotnetCorePluginBuild)/statusIcon.svg" alt=""/></a>

The TeamCity .NET plugin provides support for [.NET CLI](https://github.com/dotnet/cli) in TeamCity.

It simplifies building windows and cross-platform applications that use .NET frameworks and libraries.

# Features

The plugin provides the following features for .NET project building:
* `.NET CLI (dotnet)` build runner
* .NET CLI detection on TeamCity build agent
* auto-discovery of build steps
* cleanup of nuget caches to meet the agent [free space requirements](https://confluence.jetbrains.com/display/TCDL/Free+disk+space)
* supports both project.json and csproj-based projects
* provide on-fly test reporting
* supports code coverage
 
# Download

* for [TeamCity 2017 and greater](https://www.jetbrains.com/teamcity/download/) you can download [plugin](https://plugins.jetbrains.com/plugin/9190?pr=teamcity) and install it as [an additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins)
* for TeamCity 9.1.x you can download [previous version of plugin](https://teamcity.jetbrains.com/repository/download/TeamCityDotnetCorePluginBuild/1179662:id/dotnet-core-plugin.zip)

# Compatibility

The current version of plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 2017 and greater.

# Configuration

## .NET CLI toolkit

To use the `dotnet` build runner, install [.NET CLI](https://www.microsoft.com/net/core) and add the .NET CLI tools path to the `PATH` environment variable.

Also, you can configure the `DOTNET_HOME` environment variable for your TeamCity build agent user, for instance:

```
DOTNET_HOME=C:\Program Files\dotnet\
```

# Build

This project uses gradle as a build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or write an issue.
