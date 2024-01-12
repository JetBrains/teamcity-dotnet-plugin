

package jetbrains.buildServer.dotnet.commands.transformation

enum class DotnetCommandsTransformationStage {
    Initial,                // composing all transformers together
    Targeting,              // multiply command by every target
    Transformation,         // transform every single command (maybe to sequence of commands)
    FinalComposition,       // final composition of command line arguments
}