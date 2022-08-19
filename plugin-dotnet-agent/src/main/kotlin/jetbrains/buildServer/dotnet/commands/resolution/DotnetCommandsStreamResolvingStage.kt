package jetbrains.buildServer.dotnet.commands.resolution

enum class DotnetCommandsStreamResolvingStage {
    Initial,
    CommandRetrieve,
    Transformation,
    Targeting,
    FinalComposition,
}