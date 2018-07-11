package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import java.io.File

data class DotnetSdk(val targetArgument: CommandLineArgument, val path: File, val version: Version)