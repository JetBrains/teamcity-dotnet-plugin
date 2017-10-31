package jetbrains.buildServer.dotnet.discovery

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Element
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE
import kotlin.coroutines.experimental.buildSequence

class DotnetRunnerDiscoveryExtension(private val _solutionDiscover: SolutionDiscover): BreadthFirstRunnerDiscoveryExtension(3) {
    override fun discoverRunnersInDirectory(dir: Element, filesAndDirs: MutableList<Element>): MutableList<DiscoveredObject> =
        discover(StreamFactoryImpl(dir.browser), getElements(filesAndDirs.asSequence()).map { it.fullName }).toMutableList()

    fun discover(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<DiscoveredTarget> {
        val solutions = _solutionDiscover.discover(streamFactory, paths).toList()
        val complexSolutions = solutions.asSequence().filter { !it.isSimple }.toList()
        val complexSolutionProjects = complexSolutions.flatMap { it.projects }.toSet()
        val simpleSolutions = solutions.asSequence().filter { it.isSimple && !complexSolutionProjects.containsAll(it.projects) }
        return complexSolutions.asSequence().plus(simpleSolutions).flatMap { createCommands(it) }.distinct().map { createTarget(it) }
    }

    private fun getElements(elements: Sequence<Element>): Sequence<Element> =
            elements.filter { it.isLeaf && it.isContentAvailable }.plus(elements.filter { !it.isLeaf && it.children != null }.flatMap { getElements(it.children!!.asSequence()) })

    private fun createTarget(command: Command): DiscoveredTarget {
        LOG.debug("Target was created \"$command\"")
        return DiscoveredTarget(command.parameters.associate { it.name to it.value })
    }

    private fun createCommands(solution: Solution): Sequence<Command> = buildSequence {
        if (!solution.solution.isNullOrBlank()) {
            var solutionPath = normalizePath(solution.solution)
            yield(Command(listOf(Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Restore.id), Parameter(DotnetConstants.PARAM_PATHS, solutionPath))))
            yield(Command(listOf(Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Build.id), Parameter(DotnetConstants.PARAM_PATHS, solutionPath))))

            if (solution.projects.filter { isTestProject(it) }.any()) {
                yield(Command(listOf(Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Test.id), Parameter(DotnetConstants.PARAM_PATHS, solutionPath))))
            }

            if (solution.projects.filter { isPublishProject(it) }.any()) {
                yield(Command(listOf(Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Publish.id), Parameter(DotnetConstants.PARAM_PATHS, solutionPath))))
            }
        }
        else {
            for (project in solution.projects) {
                if (project.project.isNullOrBlank()) {
                    continue
                }

                var projectPath = normalizePath(project.project)
                yield(Command(listOf(Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Restore.id), Parameter(DotnetConstants.PARAM_PATHS, projectPath))))

                if (isTestProject(project)) {
                    yield(Command(listOf(Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Test.id), Parameter(DotnetConstants.PARAM_PATHS, projectPath))))
                    continue
                }

                if (isPublishProject(project)) {
                    yield(Command(listOf(Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Publish.id), Parameter(DotnetConstants.PARAM_PATHS, projectPath))))
                    continue
                }

                yield(Command(listOf(Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Build.id), Parameter(DotnetConstants.PARAM_PATHS, projectPath))))
            }
        }
    }

    private fun isTestProject(project: Project): Boolean = project.references.filter { TestReferencePattern.matcher(it.id).find() }.any()

    private fun isPublishProject(project: Project): Boolean = project.generatePackageOnBuild || project.references.filter { PublishReferencePattern.matcher(it.id).find() }.any()

    private fun normalizePath(path: String): String = path.replace('\\', '/')

    private data class Command(private val _parameters: List<Parameter>) {
        val parameters: List<Parameter>

        init {
            parameters = _parameters.sortedBy { it.name }
        }
    }

    private data class Parameter(val name: String, val value: String) { }

    private companion object {
        private val LOG: Logger = Logger.getInstance(DotnetRunnerDiscoveryExtension::class.java.name)
        private val PublishReferencePattern: Pattern = Pattern.compile("Microsoft\\.aspnet.*", CASE_INSENSITIVE)
        private val TestReferencePattern: Pattern = Pattern.compile("Microsoft\\.NET\\.Test\\.Sdk", CASE_INSENSITIVE)
    }
}