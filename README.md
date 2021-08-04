# .NET Plugins for [<img src="https://cdn.worldvectorlogo.com/logos/teamcity.svg" height="20" align="center" alt="TeamCity" />](https://www.jetbrains.com/teamcity/)

[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build](http://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NetPlugin_NetDev)/statusIcon.svg)](http://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NetPlugin_NetDev&guest=1)

# Plugins

* [.NET plugin](#net-plugin)
* [C# Script runner](#c-script-runner)
* [Inspections](#inspections)
* [Duplicates Finder](#duplicates-finder)

## .NET plugin

The TeamCity .NET plugin provides support for .NET tools in TeamCity. It simplifies building Windows and cross-platform applications that use .NET frameworks and libraries.

### Features

The plugin provides the following features:
* .NET build runner for [.NET CLI](https://docs.microsoft.com/en-us/dotnet/core/tools/dotnet), [MSBuild](https://docs.microsoft.com/en-us/visualstudio/msbuild/msbuild), [Visual Studio Test](https://docs.microsoft.com/en-us/visualstudio/test/vstest-console-options) and [Visual Studio Devenv](https://docs.microsoft.com/en-us/visualstudio/ide/reference/devenv-command-line-switches)
* [.NET SDK](https://docs.microsoft.com/en-us/dotnet/core/sdk) and .NET tools detection on TeamCity build agents
* Auto-discovery of build steps
* On-the-fly test reporting
* Supports code coverage tools
* Cleanup of NuGet caches to meet the agent [free space requirements](https://www.jetbrains.com/help/teamcity/?Free+disk+space)
* Supports [TeamCity Docker Wrapper](https://www.jetbrains.com/help/teamcity/docker-wrapper.html)

### Compatibility

The plugin is compatible with

- [TeamCity 2017.1+](https://www.jetbrains.com/teamcity/download/)
- [.NET CLI 1.0+](https://dotnet.microsoft.com/download/dotnet-core/)
- Visual Studio 2013+
- MSBuild 12+
- VSTest console 12+

### .NET CLI toolkit

To use the `dotnet` build runner, install [.NET CLI](https://dotnet.microsoft.com/download/dotnet-core/) and add the .NET CLI tools path to the `PATH` environment variable.

You can also configure the `DOTNET_HOME` environment variable for your TeamCity build agent user, for instance:

```
DOTNET_HOME=C:\Program Files\dotnet\
```

### Known issues

Tests might not be reported correctly for .NET Core xunit test projects when logging verbosity levels is `minimal` or `quiet` because of [issue](https://github.com/xunit/xunit/issues/1706).

## C# Script runner

### Features

* Allows executing a C# script on Windows, Linux, or macOS
* Cleanup of NuGet caches to meet the agent [free space requirements](https://www.jetbrains.com/help/teamcity/?Free+disk+space)
* Supports [TeamCity Docker Wrapper](https://www.jetbrains.com/help/teamcity/docker-wrapper.html)

### Compatibility

The plugin is compatible with

- [.NET runtime 3.1](https://dotnet.microsoft.com/download/dotnet/3.1)

## Inspections

The Inspections (ReSharper) build runner allows you to use the benefits of the [JetBrains ReSharper code quality analysis](https://www.jetbrains.com/help/resharper/Code_Analysis__Index.html) feature right in TeamCity, with the help of the bundled JetBrains ReSharper Command Line Tools. You can use the tools within TeamCity without any additional ReSharper license. ReSharper analyzes your C#, VB.NET, XAML, XML, ASP.NET, ASP.NET MVC, JavaScript, HTML, CSS code, and allows you to:

* Find probable bugs
* Eliminate errors and code smells
* Detect performance issues
* Improve the code structure and maintainability
* Ensure the code conforms to guidelines, standards and specifications

## Duplicates Finder

The Duplicates finder (ReSharper) build runner, based on [ReSharper Command Line Tools](https://www.jetbrains.com/resharper/features/command-line.html), is intended to catch similar code fragments and provide a report on the discovered repetitive blocks of C# and Visual Basic .NET code in Visual Studio 2003, 2005, 2008, 2010, 2012, 2013, and 2015 solutions.

## Additional Resources

* [TeamCity .NET](https://www.jetbrains.com/help/teamcity/net.html)
* [New approach and demo](https://blog.jetbrains.com/teamcity/2020/12/teamcity-integration-with-net-part-1-new-approach-and-demo/)
* [Testing and building projects](https://blog.jetbrains.com/teamcity/2020/12/teamcity-integration-with-net-part-2-testing-and-building-projects/)
* [Deploying projects](https://blog.jetbrains.com/teamcity/2020/12/teamcity-integration-with-net-part-3-deploying-projects/)

## Build

This project uses gradle as a build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

## Report and Track Issues

Please use our YouTrack to [report](https://youtrack.jetbrains.com/newIssue?project=TW&description=Expected%20behavior%20and%20actual%20behavior%3A%0A%0ASteps%20to%20reproduce%20the%20problem%3A%0A%0ASpecifications%20like%20the%20tool%20version%2C%20operating%20system%3A%0A%0AResult%20of%20'dotnet%20--info'%3A&c=Subsystem%20Agent%20-%20.NET&c=Assignee%20Nikolay.Pianikov&c=tag%20.NET%20Core) related issues.
