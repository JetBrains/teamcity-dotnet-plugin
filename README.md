# TeamCity .NET Core Plugin

<a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityDotnetCorePluginBuild&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityDotnetCorePluginBuild)/statusIcon.svg" alt=""/></a>

TeamCity .NET Core plugin brings support of [.NET CLI](https://github.com/dotnet/cli) toolchain.

It simplifies build of cross-platform applications which use frameworks and libraries like [ASP.NET Core](https://github.com/aspnet/Home) and [EF Core](https://github.com/aspnet/EntityFramework).

# Features

It provides following features for .NET Core project building:
* `dotnet` command build runner
* .NET Core tools detection at build agents
* auto-discovery of build steps
* cleanup of nuget caches to meet agent [free space requirement](https://confluence.jetbrains.com/display/TCDL/Free+disk+space)
 
# Download

You can download plugin from the [last successful build](https://teamcity.jetbrains.com/repository/download/TeamCityDotnetCorePluginBuild/.lastSuccessful/dotnet-core-plugin.zip?guest=1) and install it as [additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

# Compatibility

Plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 9.1.x and greater.

# Configuration

## .NET CLI toolkit

To use `dotnet` build runner [install .NET CLI](http://dotnet.github.io/getting-started/) and add bin directory of .NET CLI tools to the `PATH` environment variable.

Also, you can configure environment variable `DOTNET_HOME` for your TeamCity build agent user, for instance:

```
DOTNET_HOME=C:\Program Files\dotnet\
```

# Build

This project uses gradle as a build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or write an issue.
