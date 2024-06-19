package jetbrains.buildServer.nunit.arguments

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.nunit.NUnitSettings
import java.io.File

class NUnitArgumentsProvider(
    private val _nUnitSettings: NUnitSettings,
    private val _argumentsService: ArgumentsService,
    private val _filterProvider: NUnitTestFilterProvider
) {
    fun createCommandLineArguments(resultFile: File) = sequence {
        yield(CommandLineArgument("$RESULT_ARG=${resultFile.absolutePath}"))
        yield(CommandLineArgument(NO_HEADER_ARG))

        val testFilter = _filterProvider.filter
        if (testFilter.isNotEmpty()) {
            yield(CommandLineArgument(WHERE_ARG))
            yield(CommandLineArgument(testFilter))
        }

        // Command line arguments
        val commandLine = _nUnitSettings.additionalCommandLine
        if (commandLine != null) {
            yieldAll(_argumentsService.split(commandLine).map { CommandLineArgument(it) })
        }
    }

    companion object {
        private const val WHERE_ARG = "--where"
        private const val RESULT_ARG = "--result"
        private const val NO_HEADER_ARG = "--noheader"
    }
}
