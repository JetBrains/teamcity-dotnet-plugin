

package jetbrains.buildServer.agent.runner

enum class PathType {
    WorkingDirectory,

    Checkout,

    AgentTemp,

    BuildTemp,

    GlobalTemp,

    Bin,

    Plugins,

    Plugin,

    Tools,

    Lib,

    Work,

    System,

    Config,

    Log,

    GlobalCache,

    Cache,

    CachePerCheckout
}