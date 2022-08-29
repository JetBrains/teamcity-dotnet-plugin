///*
// * Copyright 2000-2022 JetBrains s.r.o.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package jetbrains.buildServer.dotnet.commands.resolution
//
//import jetbrains.buildServer.agent.*
//import jetbrains.buildServer.agent.runner.*
//import jetbrains.buildServer.dotnet.*
//import jetbrains.buildServer.rx.subscribe
//import jetbrains.buildServer.rx.use
//
//class DotnetListTestsStateWorkflowComposer(
//    private val _listTestsDotnetCommand: DotnetCommand,
//    private val _pathsService: PathsService,
//    private val _virtualContext: VirtualContext,
//    private val _pathResolverWorkflowComposers: List<PathResolverWorkflowComposer>,
//    private val _defaultEnvironmentVariables: EnvironmentVariables,
//    private val _pathService: PathsService,
//    private val _fileSystemService: FileSystemService,
//    private val _parametersService: ParametersService,
//    private val _splitTestsFilterSettings: SplittedTestsFilterSettings,
//) : DotnetCommandsStreamWorkflowComposer {
//    private val useExactMatchFilter: Boolean get() =
//        _parametersService
//            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_USE_EXACT_MATCH_FILTER)
//            ?.trim()
//            ?.let { it.equals("true", true) }
//            ?: false
//
//    private fun shouldBeApplied(commands: DotnetCommandsStream) =
//        _splitTestsFilterSettings.IsActive && useExactMatchFilter && commands.any { it is TestCommand }
//
//    override val target = TargetType.ToolTargetDiagnostics
//
//    override fun compose(context: WorkflowContext, state: DotnetCommandsStreamState, workflow: Workflow): Workflow = sequence {
//        if (!shouldBeApplied(state.commands)) {
//            return@sequence
//        }
//
//        val testsListFile = _pathsService.getTempFileName(ListTestsFileExtension)
//
//        context
//            .toExitCodes()
//            .subscribe { exitCode -> if (exitCode != 0) context.abort(BuildFinishedStatus.FINISHED_FAILED) }
//            .use {
//                yield(
//                    CommandLine(
//                        baseCommandLine = null,
//                        target = target,
//                        executableFile = when {
//                            _virtualContext.isVirtual && state.toolPath.homePaths.isEmpty() -> state.toolPath.virtualPath
//                            else -> state.toolPath.path
//                        },
//                        workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath),
//                        arguments = listOf(
//                            "--list-tests",
//                            "--",
//                            "NUnit.DisplayName=FullName",   // NUnit should be set up to print fully qualified names
//                        ).map(::CommandLineArgument),
//                        environmentVariables = _defaultEnvironmentVariables.getVariables(Version.Empty).toList(),
//                        title = "dotnet --list-tests",
//                        description = listOf(StdOutText("List targets' tests")),
//                        outputFile = testsListFile
//                    )
//                )
//            }
//    }.let { Workflow(it) }
//
//    companion object {
//        private const val ListTestsFileExtension = ".tests"
//    }
//}