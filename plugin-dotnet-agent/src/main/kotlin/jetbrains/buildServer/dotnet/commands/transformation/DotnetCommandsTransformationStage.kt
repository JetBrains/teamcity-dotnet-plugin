package jetbrains.buildServer.dotnet.commands.transformation

enum class DotnetCommandsTransformationStage {
    Initial,                // composing all transformers together
    Targeting,              // multiply command by every target
    DepCacheRestoration,    // prepare data for the dependency cache, restore it and update invalidation data
    Splitting,              // splits tests into parallel batches
    Retry,                  // failed tests retry
    FinalComposition,       // final composition of command line arguments
}