## .NET CLI Plugin for [<img src="https://cdn.worldvectorlogo.com/logos/teamcity.svg" height="20" align="center" alt="TeamCity" />](https://www.jetbrains.com/teamcity/)

[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

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

You can [download the plugin](https://plugins.jetbrains.com/plugin/9190?pr=teamcity) and install it as [an additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins). The latest plugin builds:

| Branch | Status | Download | TeamCity |
|--------|--------|----------|----------|
| master | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_DotnetCLI_20172&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_DotnetCLI_20172),branch:master/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_DotnetCLI_20172/.lastSuccessful/dotnet.cli.zip?branch=master&guest=1)| 2017.2.x |
| Indore-2017.1.x | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityDotnetCorePluginBuild&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityDotnetCorePluginBuild),branch:Indore-2017.1.x/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityDotnetCorePluginBuild/.lastSuccessful/dotnet-cli.zip?branch=Indore-2017.1.x&guest=1)| 2017.1.x |

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

We appreciate all kinds of feedback, so please feel free to send a PR or file an issue in the [TeamCity tracker](https://youtrack.jetbrains.com/newIssue?project=TW&clearDraft=true&summary=.NET%20CLI:&c=Subsystem%20Agent%20-%20.NET&c=tag%20.NET%20Core).
