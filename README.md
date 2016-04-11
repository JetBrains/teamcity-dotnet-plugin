# Overview

<a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityDotnetCorePluginBuild&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityDotnetCorePluginBuild)/statusIcon" alt=""/></a>

TeamCity .NET Core plugin brings support of [ASP.NET DNX](http://docs.asp.net/en/latest/dnx/overview.html) and [.NET CLI](https://github.com/dotnet/cli) tools.

It simplifies build of cross-platform applications which use frameworks and libraries like ASP.NET Core and EF Core.

# Features

It provides following features for .NET Core project building:
* `dnu` command build runner
* `dnx` command build runner
* `dotnet` command build runner
* DNX runtime detection at the build agent
* Auto-discovery of the build steps.
 
# Download

You can download plugin from the [last successful build](https://teamcity.jetbrains.com/repository/download/TeamCityDotnetCorePluginBuild/.lastSuccessful/dotnet-plugin.zip?guest=1) and install it as [additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

# Compatibility

Plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 9.1.x and greater.

# Configuration

## DNX tools

To use `dnu` and `dnx` build runners you should install DNX tools according to the [Getting Started guide](https://docs.asp.net/en/latest/getting-started/index.html) and add DNX runtime directory to the `PATH` environment variable. 

Also, you can use `DNX_PATH` environment variable to specify required DNX tools, for instance:

```
DNX_PATH=%username%\.dnx\runtimes\dnx-coreclr-win-x64.1.0.0-rc2-16357\bin\dnx.exe
```

## .NET CLI toolkit

To use `dotnet` build runner [install .NET CLI](http://dotnet.github.io/getting-started/) and add bin directory of .NET CLI tools to the `PATH` environment variable.

Also you configure environment variable `DOTNET_HOME` for your TeamCity build agent user, for instance:

```
DOTNET_HOME=C:\Program Files\dotnet\
```

# Build

This project uses gradle as a build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or write an issue.
