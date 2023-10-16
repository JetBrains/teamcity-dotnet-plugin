/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

package jetbrains.buildServer.dotcover

import com.fasterxml.jackson.core.util.VersionUtil
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.util.VersionComparatorUtil
import java.io.File

class DotCoverWorkflowComposer(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
    private val _fileSystemService: FileSystemService,
    private val _dotCoverProjectSerializer: DotCoverProjectSerializer,
    private val _loggerService: LoggerService,
    private val _argumentsService: ArgumentsService,
    private val _coverageFilterProvider: CoverageFilterProvider,
    private val _virtualContext: VirtualContext,
    private val _environmentVariables: EnvironmentVariables,
) : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.CodeCoverageProfiler

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow): Workflow {
        if (!dotCoverEnabled || dotCoverPath.isBlank()) {
            return workflow
        }

        val entryPoint = selectEntryPoint().getOrElse { throw it }

        return when {
            _virtualContext.isVirtual || isEntryPointValid(entryPoint) -> Workflow(createDotCoverCommandLine(workflow, context, entryPoint))

            else -> {
                val errorMessage =
                    "Code coverage cannot be collected: " +
                    "dotCover cannot be run because the required " +
                    "runtime is not detected on the agent: ${entryPoint.requirement!!.errorMessage}"
                _loggerService.writeBuildProblem("dotCover_requirements_have_not_been_met", BuildProblemData.TC_ERROR_MESSAGE_TYPE, errorMessage)
                workflow
            }
        }
    }

    private fun isEntryPointValid(entryPoint: EntryPoint) =
        entryPoint.requirement == null || entryPoint.requirement.let(::areRequirementsSatisfied)

    private fun areRequirementsSatisfied(requirement: MinVersionConfigParameterRequirement) =
        _parametersService.getParameterNames(ParameterType.Configuration).any { requirement.validateConfigParameter(it) }

    private fun createDotCoverCommandLine(
        workflow: Workflow,
        context: WorkflowContext,
        entryPoint: EntryPoint
    ) = sequence {
        var dotCoverHome = false
        for (baseCommandLine in workflow.commandLines) {
            if (!baseCommandLine.chain.any { it.target == TargetType.Tool }) {
                yield(baseCommandLine)
                continue
            }

            val configFile = _pathsService.getTempFileName(DotCoverConfigExtension)
            val snapshotFile = _pathsService.getTempFileName(DotCoverSnapshotExtension)
            val virtualWorkingDirectory = Path(_virtualContext.resolvePath(baseCommandLine.workingDirectory.path))
            val virtualConfigFilePath = Path(_virtualContext.resolvePath(configFile.path))
            val virtualSnapshotFilePath = Path(_virtualContext.resolvePath(snapshotFile.path))

            val dotCoverProject = DotCoverProject(
                CommandLine(
                    baseCommandLine,
                    baseCommandLine.target,
                    baseCommandLine.executableFile,
                    virtualWorkingDirectory,
                    baseCommandLine.arguments,
                    baseCommandLine.environmentVariables,
                    baseCommandLine.title,
                    baseCommandLine.description
                ),
                virtualConfigFilePath,
                virtualSnapshotFilePath
            )

            _fileSystemService.write(configFile) {
                _dotCoverProjectSerializer.serialize(dotCoverProject, it)
            }

            _loggerService.writeTraceBlock("dotCover settings").use {
                val args = _argumentsService.combine(baseCommandLine.arguments.map { it.value }.asSequence())
                _loggerService.writeTrace("Command line:")
                _loggerService.writeTrace("  \"${baseCommandLine.executableFile.path}\" $args")

                _loggerService.writeTrace("Filters:")
                for (filter in _coverageFilterProvider.filters) {
                    _loggerService.writeTrace("  $filter")
                }

                _loggerService.writeTrace("Attribute Filters:")
                for (filter in _coverageFilterProvider.attributeFilters) {
                    _loggerService.writeTrace("  $filter")
                }
            }

            context.toExitCodes().subscribe {
                if (_fileSystemService.isExists(snapshotFile)) {
                    // Overrides the dotCover home path once
                    if (!dotCoverHome) {
                        _loggerService.writeMessage(DotCoverServiceMessage(Path(dotCoverPath)))
                        dotCoverHome = true
                    }

                    // The snapshot path should be virtual because of the docker wrapper converts it back
                    _loggerService.importData(DotCoverDataProcessorType, virtualSnapshotFilePath, DotCoverToolName)
                }
            }.use {
                yield(
                    CommandLine(
                        baseCommandLine = baseCommandLine,
                        target = target,
                        executableFile = Path(_virtualContext.resolvePath(entryPoint.file.path)),
                        workingDirectory = baseCommandLine.workingDirectory,
                        arguments = createArguments(dotCoverProject).toList(),
                        environmentVariables = baseCommandLine.environmentVariables + _environmentVariables.getVariables(),
                        title = baseCommandLine.title
                    )
                )
            }
        }
    }


    private val dotCoverEnabled
        get(): Boolean {
            _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)?.let {
                if (it == CoverageConstants.PARAM_DOTCOVER) return true
            }

            _parametersService.tryGetParameter(ParameterType.Runner, "dotNetCoverage.dotCover.enabled")?.let {
                if (it.trim().toBoolean()) return true
            }

            return false
        }

    private fun createArguments(dotCoverProject: DotCoverProject) = sequence {
        yield(CommandLineArgument("cover", CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument(dotCoverProject.configFile.path, CommandLineArgumentType.Target))
        yield(CommandLineArgument("${argumentPrefix}ReturnTargetExitCode"))
        yield(CommandLineArgument("${argumentPrefix}AnalyzeTargetArguments=false"))

        _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)?.let {
            val argPrefix = when(_virtualContext.targetOSType) {
                OSType.WINDOWS -> "/"
                else -> "--"
            }

            val logFileName = _virtualContext.resolvePath(_fileSystemService.generateTempFile(File(it), "dotCover", ".log").canonicalPath)
            yield(CommandLineArgument("${argPrefix}LogFile=${logFileName}", CommandLineArgumentType.Infrastructural))
        }

        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)?.let {
            _argumentsService.split(it).forEach {
                yield(CommandLineArgument(it, CommandLineArgumentType.Custom))
            }
        }
    }

    private fun selectEntryPoint(): Result<EntryPoint> {
        val entryPointFileExe = EntryPointType.WindowsExecutable.getEntryPointFile(dotCoverPath)
        val entryPointFileDll = EntryPointType.UsingAgentDotnetRuntime.getEntryPointFile(dotCoverPath)
        val entryPointFileSh = EntryPointType.UsingBundledDotnetRuntime.getEntryPointFile(dotCoverPath)

        return when {
            // on Windows
            _virtualContext.targetOSType == OSType.WINDOWS -> when {
                _fileSystemService.isExists(entryPointFileExe) -> when {
                    // cross-platform version on Windows requires .NET Framework 4.7.2+
                    _fileSystemService.isExists(entryPointFileDll) -> Result.success(EntryPoint(entryPointFileExe, MinVersionConfigParameterRequirement.DotnetFramework472))

                    // Windows-only version using agent requirements mechanism – no build-time requirements validation needed
                    else -> Result.success(EntryPoint(entryPointFileExe))
                }

                else -> Result.failure(RunBuildException("dotCover has been run on Windows, however ${EntryPointType.WindowsExecutable.entryPointFileName} wasn't found"))
            }

            // on *nix-like OS
            else -> when {
                // deprecated cross-platform version
                _fileSystemService.isExists(entryPointFileSh) -> Result.success(EntryPoint(entryPointFileSh))

                // cross-platform version on Linux/macOs requires .NET Core 3.1+
                _fileSystemService.isExists(entryPointFileDll) -> Result.success(EntryPoint(entryPointFileDll, MinVersionConfigParameterRequirement.DotnetCore31))

                else -> Result.failure(RunBuildException("Cross-platform dotCover is required"))
            }
        }
    }

    private val argumentPrefix get () =
        when(_virtualContext.targetOSType) {
            OSType.WINDOWS -> "/"
            else -> "--"
        }

    private val dotCoverPath get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
            .let { when (it.isNullOrBlank()) {
                true -> ""
                false -> it
            }}

    private data class EntryPoint(val file: File, val requirement: MinVersionConfigParameterRequirement? = null)

    private enum class EntryPointType(val entryPointFileName: String) {
        // dotCover.exe ... – simple run of Windows executable file
        WindowsExecutable("dotCover.exe"),

        // dotCover.sh ... – dotCover will select the proper bundled runtime in its own package
        UsingBundledDotnetRuntime("dotCover.sh"),

        // dotnet dotCover.dll ... – will use detected dotnet on agent
        UsingAgentDotnetRuntime("dotCover.dll");

        fun getEntryPointFile(basePath: String): File = File(basePath, this.entryPointFileName)
    }

    private enum class MinVersionConfigParameterRequirement(
        val prefix: String,
        val minVersion: String,
        val suffix: String,
        val errorMessage: String
    ) {
        DotnetFramework472(
            CONFIG_PREFIX_DOTNET_FAMEWORK,
            "4.7.2",
            CONFIG_SUFFIX_PATH,
            "cross-platform dotCover requires a minimum of .NET Framework 4.7.2+ on Windows agent"
        ),

        DotnetCore31(
            CONFIG_PREFIX_CORE_RUNTIME,
            "3.1",
            CONFIG_SUFFIX_PATH,
            "cross-platform dotCover requires a minimum of .NET Core 3.1+ on Linux or macOS agent"
        );

        private val regexPattern = """^$prefix(\d+\.\d+\.\d+)$suffix$""".toRegex()

        fun validateConfigParameter(input: String): Boolean {
            val matchResult = regexPattern.find(input) ?: return false
            val extractedVersion = matchResult.groupValues[1]
            return VersionComparatorUtil.compare(extractedVersion, minVersion) >= 0
        }

    }

    private val dotnetCliDetected get() =
        _parametersService.tryGetParameter(ParameterType.Configuration, CONFIG_SUFFIX_DOTNET_CLI_PATH)?.trim()?.isNotBlank() ?: false

    companion object {
        internal const val DotCoverDataProcessorType = CoverageConstants.COVERAGE_TYPE
        internal const val DotCoverToolName = "dotcover"
        internal const val DotCoverConfigExtension = "dotCover.xml"
        internal const val DotCoverSnapshotExtension = ".dcvr"
    }
}