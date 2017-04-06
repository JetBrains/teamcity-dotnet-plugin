# TeamCity .NET Core Plugin

<a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityDotnetCorePluginBuild&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityDotnetCorePluginBuild)/statusIcon.svg" alt=""/></a>

The TeamCity .NET Core plugin provides support of the [.NET CLI](https://github.com/dotnet/cli) toolchain.

It simplifies building cross-platform applications that use frameworks and libraries like [ASP.NET Core](https://github.com/aspnet/Home) and [EF Core](https://github.com/aspnet/EntityFramework).

# Features

The plugin provides the following features for .NET Core project building:
* the `dotnet` command build runner
* .NET Core tools detection on build agents
* auto-discovery of build steps
* cleanup of nuget caches to meet the agent [free space requirements](https://confluence.jetbrains.com/display/TCDL/Free+disk+space)
* supports both project.json and csproj-based projects
 
# Download

You can [download plugin](https://plugins.jetbrains.com/plugin/9190?pr=teamcity) and install it as [an additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

# Compatibility

The plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 9.1.x and greater.

# Configuration

## .NET CLI toolkit

To use the `dotnet` build runner, [install .NET CLI](https://www.microsoft.com/net/core) and add the .NET CLI tools path to the `PATH` environment variable.

Also, you can configure the `DOTNET_HOME` environment variable for your TeamCity build agent user, for instance:

```
DOTNET_HOME=C:\Program Files\dotnet\
```

## Test Reporting

To use tests reporting for all test frameworks you need to add a reference to the [`TeamCity.VSTest.TestAdapter`](https://www.nuget.org/packages/TeamCity.VSTest.TestAdapter) nuget package in your test projects. For more details see [TeamCity.VSTest.TestAdapter](https://github.com/JetBrains/TeamCity.VSTest.TestAdapter) repository.

# Build

This project uses gradle as a build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or write an issue.
