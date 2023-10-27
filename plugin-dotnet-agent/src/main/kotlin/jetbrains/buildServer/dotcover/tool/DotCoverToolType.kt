package jetbrains.buildServer.dotcover.tool

enum class DotCoverToolType {
    // JetBrains.dotCover.CommandLine NuGet package before 2023.3
    WindowsOnly,

    // JetBrains.dotCover.CommandLine NuGet package since 2023.3
    CrossPlatform,

    // JetBrains.dotCover.DotNetCli NuGet package
    DeprecatedCrossPlatform,

    Unknown,
}