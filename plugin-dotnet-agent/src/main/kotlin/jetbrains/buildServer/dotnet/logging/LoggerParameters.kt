

package jetbrains.buildServer.dotnet.logging

import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.Verbosity

interface LoggerParameters {
    val paramVerbosity: Verbosity?

    val msBuildLoggerVerbosity: Verbosity?

    val vsTestVerbosity: Verbosity

    val msBuildParameters: String

    fun getAdditionalLoggerParameters(context: DotnetCommandContext): Sequence<String>
}