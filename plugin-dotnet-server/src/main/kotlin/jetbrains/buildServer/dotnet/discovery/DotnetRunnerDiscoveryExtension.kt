/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.discovery

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.util.browser.Element
import java.io.File

class DotnetRunnerDiscoveryExtension(
        private val _solutionDiscover: SolutionDiscover,
        private val _defaultDiscoveredTargetNameFactory: DiscoveredTargetNameFactory,
        private val _projectTypeSelector: ProjectTypeSelector,
        private val _sdkWizard: SdkWizard)
    : BreadthFirstRunnerDiscoveryExtension() {
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
        val solutions = _solutionDiscover.discover(streamFactory, paths)
        val complexSolutions = solutions.asSequence().filter { !it.isSimple }.toList()
        val complexSolutionProjects = complexSolutions.flatMap { it.projects }.toSet()
        val simpleSolutions = solutions.asSequence().filter { it.isSimple && !complexSolutionProjects.containsAll(it.projects) }
        val commands = complexSolutions.asSequence().plus(simpleSolutions).flatMap { createCommands(it) }.distinct().toList()
        return commands.asSequence().map { createTarget(it) }
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
        return DiscoveredTarget(DotnetConstants.RUNNER_TYPE, command.name, command.parameters.associate { it.name to it.value })
    }

    private fun createCommands(solution: Solution): Sequence<Command> = sequence {
        if (!solution.solution.isBlank()) {
            val isNativeOnly = solution.projects.any { isNativeOnly(it) }
            val solutionPath = normalizePath(solution.solution)
            val sdks = _sdkWizard.suggestSdks(solution.projects.asSequence(), true).toList()
            if (!isNativeOnly && solution.projects.any { requiresRestoreCommand(it) }) {
                yield(createSimpleCommand(DotnetCommandType.Restore, solutionPath, sdks))
            }

            if (!isNativeOnly) {
                yield(createSimpleCommand(DotnetCommandType.Build, solutionPath, sdks))
            }

            if (isNativeOnly || solution.projects.any { isNative(it) }) {
                yield(createMSBuildNativeCommand(solutionPath, sdks))
            }

            // If all projects contain tests
            if (!isNativeOnly && solution.projects.map { _projectTypeSelector.select(it) }.all { it.contains(ProjectType.Test) }) {
                yield(createSimpleCommand(DotnetCommandType.Test, solutionPath, sdks))
            }
        } else {
            for (project in solution.projects) {
                if (project.project.isBlank()) {
                    continue
                }

                val sdks = _sdkWizard.suggestSdks(sequenceOf(project), true).toList()
                val isNativeOnly = isNativeOnly(project)
                val projectPath = normalizePath(project.project)
                if (!isNativeOnly && requiresRestoreCommand(project)) {
                    yield(createSimpleCommand(DotnetCommandType.Restore, projectPath, sdks))
                }

                val projectTypes = _projectTypeSelector.select(project)
                if (isNativeOnly || isNative(project)) {
                    yield(createMSBuildNativeCommand(projectPath, sdks))
                }

                if (!isNativeOnly && projectTypes.contains(ProjectType.Unknown)) {
                    yield(createSimpleCommand(DotnetCommandType.Build, projectPath, sdks))
                }
            }
        }

        val testAssemblies = mutableSetOf<String>()
        for (project in solution.projects) {
            if (project.project.isBlank()) {
                continue
            }

            val projectPath = normalizePath(project.project)
            val isNativeOnly = isNativeOnly(project)
            val projectTypes = _projectTypeSelector.select(project)

            if ((isNativeOnly || isNative(project)) && projectTypes.contains(ProjectType.Test)) {
                project.properties.firstOrNull { "AssemblyName".equals(it.name, true) }?.let {
                    testAssemblies.add(normalizePath(File(projectPath).parent + "/bin/**/" + it.value + ".dll"))
                }
            }
            if (!isNativeOnly) {
                val sdks = _sdkWizard.suggestSdks(sequenceOf(project), true).toList()
                if (projectTypes.contains(ProjectType.Test)) {
                    yield(createSimpleCommand(DotnetCommandType.Test, projectPath, sdks))
                    continue
                }

                if (projectTypes.contains(ProjectType.Publish)) {
                    yield(createSimpleCommand(DotnetCommandType.Publish, projectPath, sdks))
                    continue
                }
            }
        }

        if (testAssemblies.any()) {
            yield(createTestNativeCommand(testAssemblies.joinToString(";")))
        }
    }

    private fun requiresRestoreCommand(project: Project) =
         project.frameworks.map { it.name.toLowerCase() }.all { it.startsWith("netcoreapp1")}

    private fun getSdkReauirement(sdks: List<SdkVersion>) =
        sdks.joinToString(" ") { it.version.toString() }

    private fun createSimpleCommand(commandType: DotnetCommandType, path: String, sdks: List<SdkVersion>): Command =
            Command(
                    createDefaultName(commandType, path),
                    sequence {
                        yield(Parameter(DotnetConstants.PARAM_COMMAND, commandType.id))
                        yield(Parameter(DotnetConstants.PARAM_PATHS, path))
                        if (sdks.any()) {
                            yield(Parameter(DotnetConstants.PARAM_REQUIRED_SDK, getSdkReauirement(sdks) ))
                        }
                    }.toList()
            )

    private fun createMSBuildNativeCommand(path: String, sdks: List<SdkVersion>): Command =
            Command(
                    createDefaultName(DotnetCommandType.MSBuild, path),
                    sequence {
                        yield(Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.MSBuild.id))
                        yield(Parameter(DotnetConstants.PARAM_PATHS, path))
                        yield(Parameter(DotnetConstants.PARAM_ARGUMENTS, "-restore -noLogo"))
                        if (sdks.any()) {
                            yield(Parameter(DotnetConstants.PARAM_REQUIRED_SDK, getSdkReauirement(sdks) ))
                        }
                        yield(Parameter(DotnetConstants.PARAM_MSBUILD_VERSION, Tool.values().filter { it.type == ToolType.MSBuild && it.bitness == ToolBitness.Any }.sortedBy { it.version }.reversed().first().id))
                    }.toList()
            )

    private fun createTestNativeCommand(path: String): Command =
            Command(
                    createDefaultName(DotnetCommandType.VSTest, path),
                    listOf(
                            Parameter(DotnetConstants.PARAM_COMMAND, DotnetCommandType.VSTest.id),
                            Parameter(DotnetConstants.PARAM_PATHS, path),
                            Parameter(DotnetConstants.PARAM_VSTEST_VERSION, Tool.values().filter { it.type == ToolType.VSTest }.sortedBy { it.version }.reversed().first().id)))

    private fun createDefaultName(commandType: DotnetCommandType, path: String): String =
            _defaultDiscoveredTargetNameFactory.createName(commandType, path)

    private fun isNative(project: Project): Boolean =
            !project.properties.any { "Sdk".equals(it.name, true) && "Microsoft.NET.Sdk".equals(it.value, true)}

    private fun isNativeOnly(project: Project): Boolean =
            project.frameworks.any { "net11".equals(it.name, true) || "net20".equals(it.name, true) || "net30".equals(it.name, true) || "net35".equals(it.name, true)}

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
        private val Params = setOf(DotnetConstants.PARAM_COMMAND, DotnetConstants.PARAM_PATHS, DotnetConstants.PARAM_ARGUMENTS, DotnetConstants.PARAM_MSBUILD_VERSION, DotnetConstants.PARAM_VSTEST_VERSION, DotnetConstants.PARAM_REQUIRED_SDK)
    }
}