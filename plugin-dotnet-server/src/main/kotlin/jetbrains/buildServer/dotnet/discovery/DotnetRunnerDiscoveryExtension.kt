@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet.discovery

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.util.browser.Element
import kotlin.coroutines.experimental.buildSequence

class DotnetRunnerDiscoveryExtension(
        private val _solutionDiscover: SolutionDiscover,
        private val _defaultDiscoveredTargetNameFactory: DiscoveredTargetNameFactory,
        private val _projectTypeSelector: ProjectTypeSelector)
    : BreadthFirstRunnerDiscoveryExtension(1) {
    override fun discoverRunnersInDirectory(dir: Element, filesAndDirs: MutableList<Element>): MutableList<DiscoveredObject> =
            discover(StreamFactoryImpl(dir.browser), getElements(filesAndDirs.asSequence()).map { it.fullName }).toMutableList()

    override fun postProcessDiscoveredObjects(
            settings: BuildTypeSettings,
            browser: Browser,
            discovered: MutableList<DiscoveredObject>): MutableList<DiscoveredObject> =
            getNewCommands(getExistingCommands(settings), getCreatedCommands(discovered))
                    .map { createTarget(it) }
                    .toMutableList()

    fun getNewCommands(existingCommands: Sequence<Command>, createdCommands: Sequence<Command>): Sequence<Command> =
            createdCommands.minus(existingCommands)

    fun getExistingCommands(settings: BuildTypeSettings): Sequence<Command> =
            settings.buildRunners
                    .filter { DotnetConstants.RUNNER_TYPE.equals(it.runType.type, true) }
                    .map { Command(it.name, extractParameters(it.parameters)) }
                    .toSet()
                    .asSequence()

    fun getCreatedCommands(discovered: MutableList<DiscoveredObject>): Sequence<Command> =
            discovered
                    .filter { it is DiscoveredTarget }
                    .map { it.let { Command(it.toString(), extractParameters(it.parameters)) } }
                    .asSequence()

    fun discover(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<DiscoveredTarget> {
        val solutions = _solutionDiscover.discover(streamFactory, paths).toList()
        val complexSolutions = solutions.asSequence().filter { !it.isSimple }.toList()
        val complexSolutionProjects = complexSolutions.flatMap { it.projects }.toSet()
        val simpleSolutions = solutions.asSequence().filter { it.isSimple && !complexSolutionProjects.containsAll(it.projects) }
        return complexSolutions.asSequence().plus(simpleSolutions).flatMap { createCommands(it) }.distinct().map { createTarget(it) }
    }

    private fun extractParameters(parameters: Map<String, String>): List<Parameter> =
            parameters.filter { Params.contains(it.key) && !it.value.isBlank() }.map { Parameter(it.key, it.value) }.toList()

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
        if (!solution.solution.isBlank()) {
            val solutionPath = normalizePath(solution.solution)
            yield(createSimpleCommand(DotnetCommandType.Restore, solutionPath))
            yield(createSimpleCommand(DotnetCommandType.Build, solutionPath))

            // If all projects contain tests
            if (solution.projects.map { _projectTypeSelector.select(it) }.all { it.contains(ProjectType.Test) }) {
                yield(createSimpleCommand(DotnetCommandType.Test, solutionPath))
            }
        } else {
            for (project in solution.projects) {
                if (project.project.isBlank()) {
                    continue
                }

                val projectPath = normalizePath(project.project)
                yield(createSimpleCommand(DotnetCommandType.Restore, projectPath))
                val projectTypes = _projectTypeSelector.select(project)
                if (projectTypes.contains(ProjectType.Unknown)) {
                    yield(createSimpleCommand(DotnetCommandType.Build, projectPath))
                }
            }
        }

        for (project in solution.projects) {
            if (project.project.isBlank()) {
                continue
            }

            val projectPath = normalizePath(project.project)
            val projectTypes = _projectTypeSelector.select(project)

            if (projectTypes.contains(ProjectType.Test)) {
                yield(createSimpleCommand(DotnetCommandType.Test, projectPath))
                continue
            }

            if (projectTypes.contains(ProjectType.Publish)) {
                yield(createSimpleCommand(DotnetCommandType.Publish, projectPath))
                continue
            }
        }
    }

    private fun createSimpleCommand(commandType: DotnetCommandType, path: String): Command =
            Command(createDefaultName(commandType, path), listOf(Parameter(DotnetConstants.PARAM_COMMAND, commandType.id), Parameter(DotnetConstants.PARAM_PATHS, path)))

    private fun createDefaultName(commandType: DotnetCommandType, path: String): String =
            _defaultDiscoveredTargetNameFactory.createName(commandType, path)

    private fun normalizePath(path: String): String = path.replace('\\', '/')

    class Command(val name: String, _parameters: List<Parameter>) {
        val parameters: List<Parameter> = _parameters.sortedBy { it.name }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Command

            if (parameters != other.parameters) return false
            return true
        }

        override fun hashCode(): Int {
            return parameters.hashCode()
        }
    }

    data class Parameter(val name: String, val value: String)

    private companion object {
        private val LOG: Logger = Logger.getInstance(DotnetRunnerDiscoveryExtension::class.java.name)
        private val Params = setOf(DotnetConstants.PARAM_COMMAND, DotnetConstants.PARAM_PATHS)
    }
}