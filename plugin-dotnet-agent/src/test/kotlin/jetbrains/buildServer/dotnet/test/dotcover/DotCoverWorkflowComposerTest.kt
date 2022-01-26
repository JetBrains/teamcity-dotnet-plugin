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

package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverWorkflowComposerTest {
    @MockK private lateinit var _pathService: PathsService
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _dotCoverProjectSerializer: DotCoverProjectSerializer
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _coverageFilterProvider: CoverageFilterProvider
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _environmentVariables: EnvironmentVariables
    @MockK private lateinit var _blockToken: Disposable
    private val _defaultVariables = sequenceOf(CommandLineEnvironmentVariable("Abc", "C"))

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _blockToken.dispose() } returns Unit
        every { _argumentsService.combine(any()) } answers { arg<Sequence<String>>(0).joinToString(arg<String>(1)) }
        every { _argumentsService.split(any()) } answers { arg<String>(0).split(" ").asSequence() }
    }

    @Test
    fun shouldBeProfilerOfCodeCoverage() {
        // Given
        val composer = createInstance(VirtualFileSystemService())

        // When

        // Then
        Assert.assertEquals(composer.target, TargetType.CodeCoverageProfiler)
    }

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        CoverageConstants.PARAM_DOTCOVER,
                        "dotCover",
                        VirtualFileSystemService()
                                .addFile(File("dotCover", "dotCover.exe"))
                                .addFile(File("snapshot000"))))
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            coverageType: String?,
            dotCoverPath: String?,
            fileSystemService: FileSystemService) {
        // Given
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path(File("wd").path)
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                null,
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File(dotCoverPath, "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        commandLine,
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))

        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                commandLine,
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false"),
                                        CommandLineArgument("--ProcessFilters=-:process1;-:process2", CommandLineArgumentType.Custom)
                                ),
                                envVars + _defaultVariables)))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns coverageType
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns dotCoverPath
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns "--ProcessFilters=-:process1;-:process2"
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path(dotCoverPath!!))) } returns Unit
        every { _loggerService.importData(DotCoverWorkflowComposer.DotCoverDataProcessorType, Path("v_snap"), DotCoverWorkflowComposer.DotCoverToolName) } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"
        every { _environmentVariables.getVariables() } returns _defaultVariables
        every { _coverageFilterProvider.attributeFilters } returns emptySequence()
        every { _coverageFilterProvider.filters } returns emptySequence()
        every { _loggerService.writeTraceBlock(any()) } returns _blockToken
        every { _loggerService.writeTrace(any()) } returns Unit

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        verify { _blockToken.dispose() }
        verify { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) }
        verify { _loggerService.importData(DotCoverWorkflowComposer.DotCoverDataProcessorType, Path("v_snap"), DotCoverWorkflowComposer.DotCoverToolName) }
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @DataProvider(name = "notComposeCases")
    fun getNotComposeCases(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf("other", "dotCover" as Any?),
                arrayOf("", "dotCover" as Any?),
                arrayOf("   ", "dotCover" as Any?),
                arrayOf(null, "dotCover" as Any?),
                arrayOf(CoverageConstants.PARAM_DOTCOVER, null as Any?),
                arrayOf(CoverageConstants.PARAM_DOTCOVER, "" as Any?),
                arrayOf(CoverageConstants.PARAM_DOTCOVER, "   " as Any?))
    }

    @Test(dataProvider = "notComposeCases")
    fun shouldReturnBaseWorkflowWhenCoverageDisabled(
            coverageType: String?,
            dotCoverPath: String?) {
        // Given
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                null,
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)

        val composer = createInstance(VirtualFileSystemService())
        val baseWorkflow = Workflow(sequenceOf(commandLine))

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns coverageType
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns dotCoverPath
        every { _parametersService.tryGetParameter(ParameterType.Runner, "dotNetCoverage.dotCover.enabled") } returns null

        val actualWorkflow = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, baseWorkflow).commandLines.toList()

        // Then
        Assert.assertEquals(actualWorkflow, baseWorkflow.commandLines.toList())
    }

    @Test
    fun shouldNotWrapNonToolTargetsByDotCover() {
        // Given
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                null,
                TargetType.SystemDiagnostics,
                executableFile,
                workingDirectory,
                args,
                envVars)

        val composer = createInstance(VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe")))
        val baseWorkflow = Workflow(sequenceOf(commandLine))

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, "dotNetCoverage.dotCover.enabled") } returns null
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"

        val actualWorkflow = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, baseWorkflow).commandLines.toList()

        // Then
        Assert.assertEquals(actualWorkflow, baseWorkflow.commandLines.toList())
    }

    @DataProvider(name = "showDiagnosticCases")
    fun getShowDiagnosticCases(): Array<Array<Verbosity>> {
        return arrayOf(
                arrayOf(Verbosity.Detailed),
                arrayOf(Verbosity.Diagnostic))
    }

    @Test
    fun shouldShowDiagnostic() {
        // Given
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                null,
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        commandLine,
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                commandLine,
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false")
                                ),
                                envVars + _defaultVariables)))
        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) } returns Unit
        every { _loggerService.importData(DotCoverWorkflowComposer.DotCoverToolName, Path("v_snap")) } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"

        every { _loggerService.writeTraceBlock("dotCover settings") } returns _blockToken
        every { _loggerService.writeTrace("Command line:") } returns Unit
        every { _loggerService.writeTrace("  \"${File("dotnet", "dotnet.exe").path}\" arg1") } returns Unit
        every { _loggerService.writeTrace("Filters:") } returns Unit
        val filter1 = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "abc")
        val filter2 = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "qwerty")
        every { _coverageFilterProvider.filters } returns sequenceOf(filter1, filter2)
        every { _loggerService.writeTrace("  $filter1") } returns Unit
        every { _loggerService.writeTrace("  $filter2") } returns Unit
        every { _loggerService.writeTrace("Attribute Filters:") } returns Unit
        val attributeFilter = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "xyz")
        every { _coverageFilterProvider.attributeFilters } returns sequenceOf(attributeFilter)
        every { _loggerService.writeTrace("  $attributeFilter") } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) } returns Unit
        every { _loggerService.writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverDataProcessorType, Path("v_snap"), DotCoverWorkflowComposer.DotCoverToolName)) } returns Unit
        every { _environmentVariables.getVariables() } returns _defaultVariables

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldNotPublishServiceMessageWhenWorkflowFailed() {
        // Given
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1", CommandLineArgumentType.Secondary))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                null,
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        commandLine,
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                commandLine,
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false")
                                ),
                                envVars + _defaultVariables)))
        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"
        every { _environmentVariables.getVariables() } returns _defaultVariables
        every { _coverageFilterProvider.attributeFilters } returns emptySequence()
        every { _coverageFilterProvider.filters } returns emptySequence()
        every { _loggerService.writeTraceBlock(any()) } returns _blockToken
        every { _loggerService.writeTrace(any()) } returns Unit

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Failed, CommandResultExitCode(0)), Unit, Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        verify(exactly = 0) { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) }
        verify(exactly = 0) { _loggerService.writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverDataProcessorType, Path("v_snap"), DotCoverWorkflowComposer.DotCoverToolName)) }

        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldTakeInAccountDotCoverArguments() {
        // Given
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                null,
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        commandLine,
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                commandLine,
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false"),
                                        CommandLineArgument("/ProcessFilters=-:sqlservr.exe", CommandLineArgumentType.Custom),
                                        CommandLineArgument("/arg", CommandLineArgumentType.Custom)
                                ),
                                envVars + _defaultVariables)))

        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns "/ProcessFilters=-:sqlservr.exe /arg"
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) } returns Unit
        every { _loggerService.importData(DotCoverWorkflowComposer.DotCoverToolName, Path("v_snap")) } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"
        every { _environmentVariables.getVariables() } returns _defaultVariables
        every { _coverageFilterProvider.attributeFilters } returns emptySequence()
        every { _coverageFilterProvider.filters } returns emptySequence()
        every { _loggerService.writeTraceBlock(any()) } returns _blockToken
        every { _loggerService.writeTrace(any()) } returns Unit

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldSupportLogFileConfigParamArguments() {
        // Given
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                null,
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        commandLine,
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                commandLine,
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false"),
                                        CommandLineArgument("/LogFile=v_log", CommandLineArgumentType.Infrastructural)
                                ),
                                envVars + _defaultVariables)))

        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns "logPath"
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) } returns Unit
        every { _loggerService.importData(DotCoverWorkflowComposer.DotCoverToolName, Path("v_snap")) } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath(File("logPath", "dotCover99.log").canonicalPath) } returns "v_log"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"
        every { _environmentVariables.getVariables() } returns _defaultVariables
        every { _coverageFilterProvider.attributeFilters } returns emptySequence()
        every { _coverageFilterProvider.filters } returns emptySequence()
        every { _loggerService.writeTraceBlock(any()) } returns _blockToken
        every { _loggerService.writeTrace(any()) } returns Unit

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldThrowExceptionWhenRequiredCrossPlatformDotCoverButCannotFindIt() {
        // Given
        val commandLine = CommandLine(
                null,
                TargetType.Tool,
                Path(File("dotnet", "dotnet").path),
                Path("wd"),
                listOf(CommandLineArgument("arg1", CommandLineArgumentType.Secondary)),
                listOf(CommandLineEnvironmentVariable("var1", "val1")))
        val fileSystemService = VirtualFileSystemService()
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.UNIX
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"

        // Then
        try {
            composer.compose(WorkflowContextStub(WorkflowStatus.Failed, CommandResultExitCode(0)), Unit, Workflow(sequenceOf(commandLine))).commandLines.toList()
            Assert.fail("Eception is required.")
        }
        catch (ex: RunBuildException) {
            Assert.assertEquals(ex.message, "Cross-Platform dotCover is required.")
        }
    }

    private fun createInstance(fileSystemService: FileSystemService): SimpleWorkflowComposer {
        return DotCoverWorkflowComposer(
                _pathService,
                _parametersService,
                fileSystemService,
                _dotCoverProjectSerializer,
                _loggerService,
                _argumentsService,
                _coverageFilterProvider,
                _virtualContext,
                _environmentVariables)
    }
}