package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineArgument
import java.io.File

data class InspectionArguments(
        val configFile: File,
        val outputFile: File,
        val logFile: File,
        val cachesHome: File,
        val debug: Boolean,
        val customArguments: Collection<CommandLineArgument>)