package jetbrains.buildServer.dotcover.tool

enum class DotCoverToolType {
    // JetBrains.dotCover.CommandLine NuGet package before 2023.3
    WindowsOnly,

    // JetBrains.dotCover.CommandLine NuGet package since 2023.3 and before 2025.2
    CrossPlatform,

    // JetBrains.dotCover.CommandLine NuGet package since 2025.2
    CrossPlatformV3,

    // JetBrains.dotCover.DotNetCli NuGet package
    DeprecatedCrossPlatform,

    Unknown,
}