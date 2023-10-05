package jetbrains.buildServer.dotnet.commands.transformation.test

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsStream

interface TestsSplittingCommandTransformer {
    val mode: TestsSplittingMode
    fun transform(testCommand: DotnetCommand): DotnetCommandsStream
}