package jetbrains.buildServer.runners

enum class PathType {
    WorkingDirectory,

    Checkout,

    AgentTemp,

    BuildTemp,

    GlobalTemp,

    Bin,

    Plugins,

    Tools,

    Lib,

    Work,

    System,

    Config,

    Log
}