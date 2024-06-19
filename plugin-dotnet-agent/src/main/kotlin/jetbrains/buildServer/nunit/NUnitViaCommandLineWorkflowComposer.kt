package jetbrains.buildServer.nunit

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.nunit.arguments.NUnitArgumentsProvider
import jetbrains.buildServer.nunit.arguments.NUnitConsoleRunnerPathProvider
import jetbrains.buildServer.nunit.arguments.NUnitTestingAssembliesProvider
import java.io.File
import kotlin.io.path.absolutePathString

class NUnitViaCommandLineWorkflowComposer(
    private val _commandLineArgumentsProvider: NUnitArgumentsProvider,
    private val _pathsService: PathsService,
    private val _nUnitSettings: NUnitSettings,
    private val _consoleRunnerPathProvider: NUnitConsoleRunnerPathProvider,
    private val _testingAssembliesProvider: NUnitTestingAssembliesProvider
) : SimpleWorkflowComposer {
    // NotApplicable because it's explicitly called only in NUnitWorkflowComposer
    override val target = TargetType.NotApplicable

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow): Workflow = sequence {
        val assemblies = _testingAssembliesProvider.assemblies
        if (assemblies.isEmpty()) {
            return@sequence
        }

        yield(
            CommandLine(
                baseCommandLine = null,
                target = TargetType.Tool,
                executableFile = Path(_consoleRunnerPathProvider.consoleRunnerPath.absolutePathString()),
                workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).path),
                arguments = getArguments(assemblies).toList()
            )
        )
    }.let(::Workflow)

    private fun getArguments(assemblies: List<File>) = sequence {
        for (assembly in assemblies) {
            yield(CommandLineArgument(assembly.path))
        }

        val appConfigFile = _nUnitSettings.appConfigFile
        if (!appConfigFile.isNullOrBlank()) {
            yield(CommandLineArgument("$CONFIG_ARG=$appConfigFile"))
        }

        val resultFile = _pathsService.getTempFileName(NUNIT_RESULT_EXT)
        yieldAll(_commandLineArgumentsProvider.createCommandLineArguments(resultFile))
    }

    companion object {
        private const val CONFIG_ARG = "--configfile"
        private const val NUNIT_RESULT_EXT = ".xml"
    }
}