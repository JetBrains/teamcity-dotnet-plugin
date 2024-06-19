package jetbrains.buildServer.nunit

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.agent.runner.serviceMessages.PublishArtifactsServiceMessage
import jetbrains.buildServer.nunit.arguments.NUnitArgumentsProvider
import jetbrains.buildServer.nunit.arguments.NUnitConsoleRunnerPathProvider
import jetbrains.buildServer.nunit.nUnitProject.NUnitProject
import jetbrains.buildServer.nunit.nUnitProject.NUnitProjectGenerator
import jetbrains.buildServer.nunit.nUnitProject.NUnitProjectSerializer
import java.io.File
import java.io.IOException
import kotlin.io.path.absolutePathString

class NUnitViaProjectFileWorkflowComposer(
    private val _commandLineArgumentsProvider: NUnitArgumentsProvider,
    private val _pathsService: PathsService,
    private val _fileSystem: FileSystemService,
    private val _loggerService: LoggerService,
    private val _consoleRunnerPathProvider: NUnitConsoleRunnerPathProvider,
    private val _projectSerializer: NUnitProjectSerializer,
    private val _projectGenerator: NUnitProjectGenerator
) : SimpleWorkflowComposer {
    // NotApplicable because it's explicitly called only in NUnitWorkflowComposer
    override val target = TargetType.NotApplicable

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow): Workflow = sequence {
        for (project in _projectGenerator.generate()) {
            val projectFile = _pathsService.getTempFileName(NUNIT_PROJECT_EXT)
            createProjectFile(projectFile, project)
            publishProjectFile(projectFile)

            yield(
                CommandLine(
                    baseCommandLine = null,
                    target = TargetType.Tool,
                    executableFile = Path(_consoleRunnerPathProvider.consoleRunnerPath.absolutePathString()),
                    workingDirectory = Path(getProjectWorkingDirectory(project).absolutePath),
                    arguments = getArguments(projectFile).toList()
                )
            )
        }
    }.let(::Workflow)

    private fun getProjectWorkingDirectory(project: NUnitProject): File {
        val workingDirectory = _pathsService.getPath(PathType.WorkingDirectory)
        val checkoutDirectory = _pathsService.getPath(PathType.Checkout)

        // if custom working directory was provided by user
        if (workingDirectory != checkoutDirectory) {
            return workingDirectory
        }
        return project.appBase
    }

    private fun getArguments(projectFile: File) = sequence {
        val resultFile = File(projectFile.toString() + NUNIT_RESULT_EXT)
        yield(CommandLineArgument(projectFile.path))
        yieldAll(_commandLineArgumentsProvider.createCommandLineArguments(resultFile))
    }

    private fun createProjectFile(file: File, project: NUnitProject) = try {
        _fileSystem.write(file) { outputStream -> _projectSerializer.create(project, outputStream) }
    } catch (ignored: IOException) {
        LOG.error(ignored)
        throw RunBuildException("Unable to create temporary file " + file.path + " required to run this build")
    }

    private fun publishProjectFile(file: File) = _loggerService
        .writeMessage(PublishArtifactsServiceMessage(file.absolutePath, "nUnitProject"))

    companion object {
        private val LOG = Logger.getInstance(NUnitViaProjectFileWorkflowComposer::class.java.name)
        private const val NUNIT_PROJECT_EXT = ".nunit"
        private const val NUNIT_RESULT_EXT = ".xml"
    }
}