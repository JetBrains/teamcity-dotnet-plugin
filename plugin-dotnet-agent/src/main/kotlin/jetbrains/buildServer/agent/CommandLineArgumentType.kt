package jetbrains.buildServer.agent

enum class CommandLineArgumentType {
    // commands
    Mandatory,

    // targets
    Target,

    // additional arguments
    Secondary,

    // user custom arguments
    Custom,

    // added by runner
    Infrastructural
}