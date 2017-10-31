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

class DotnetRunnerDiscoveryExtension(
        private val _solutionDiscover: SolutionDiscover,
        private val _defaultDiscoveredTargetNameFactory: DiscoveredTargetNameFactory)
    : BreadthFirstRunnerDiscoveryExtension(1) {
    override fun discoverRunnersInDirectory(dir: Element, filesAndDirs: MutableList<Element>): MutableList<DiscoveredObject> =
        discover(StreamFactoryImpl(dir.browser), getElements(filesAndDirs.asSequence()).map { it.fullName }).toMutableList()

    fun discover(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<DiscoveredTarget> {
        val solutions = _solutionDiscover.discover(streamFactory, paths).toList()
        val complexSolutions = solutions.asSequence().filter { !it.isSimple }.toList()
        val complexSolutionProjects = complexSolutions.flatMap { it.projects }.toSet()
        val simpleSolutions = solutions.asSequence().filter { it.isSimple && !complexSolutionProjects.containsAll(it.projects) }
        return complexSolutions.asSequence().plus(simpleSolutions).flatMap { createCommands(it) }.distinct().map { createTarget(it) }
    }

    private fun getElements(elements: Sequence<Element>, depth: Int = 3): Sequence<Element> =
            if (depth > 0)
                elements.filter { it.isLeaf && it.isContentAvailable }.plus(elements.filter { !it.isLeaf && it.children != null }.flatMap { getElements(it.children!!.asSequence(), depth - 1) })
            else
                emptySequence()

    private fun createTarget(command: Command): DiscoveredTarget {
        LOG.debug("Target was created \"$command\"")
        return DiscoveredTarget(command.name, command.parameters.associate { it.name to it.value })
    }

    private fun createCommands(solution: Solution): Sequence<Command> = buildSequence {
        if (!solution.solution.isNullOrBlank()) {
            var solutionPath = normalizePath(solution.solution)
            yield(createSimpleCommand(DotnetCommandType.Restore, solutionPath))
            yield(createSimpleCommand(DotnetCommandType.Build, solutionPath))

            if (solution.projects.filter { isTestProject(it) }.any()) {
                yield(createSimpleCommand(DotnetCommandType.Test, solutionPath))
            }

            if (solution.projects.filter { isPublishProject(it) }.any()) {
                yield(createSimpleCommand(DotnetCommandType.Publish, solutionPath))
            }
        }
        else {
            for (project in solution.projects) {
                if (project.project.isNullOrBlank()) {
                    continue
                }

                var projectPath = normalizePath(project.project)
                yield(createSimpleCommand(DotnetCommandType.Restore, projectPath))

                if (isTestProject(project)) {
                    yield(createSimpleCommand(DotnetCommandType.Test, projectPath))
                    continue
                }

                if (isPublishProject(project)) {
                    yield(createSimpleCommand(DotnetCommandType.Publish, projectPath))
                    continue
                }

                yield(createSimpleCommand(DotnetCommandType.Build, projectPath))
            }
        }
    }

    private fun createSimpleCommand(commandType: DotnetCommandType, path:String): Command =
        Command(createDefaultName(commandType, path), listOf(Parameter(DotnetConstants.PARAM_COMMAND, commandType.id), Parameter(DotnetConstants.PARAM_PATHS, path)))

    private fun createDefaultName(commandType: DotnetCommandType, path:String): String =
            _defaultDiscoveredTargetNameFactory.createName(commandType, path)

    private fun isTestProject(project: Project): Boolean = project.references.filter { TestReferencePattern.matcher(it.id).find() }.any()

    private fun isPublishProject(project: Project): Boolean = project.generatePackageOnBuild || project.references.filter { PublishReferencePattern.matcher(it.id).find() }.any()

    private fun normalizePath(path: String): String = path.replace('\\', '/')

    private data class Command(val name: String, private val _parameters: List<Parameter>) {
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