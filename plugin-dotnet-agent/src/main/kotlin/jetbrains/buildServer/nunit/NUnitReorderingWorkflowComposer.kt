package jetbrains.buildServer.nunit

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.agent.runner.serviceMessages.PublishArtifactsServiceMessage
import jetbrains.buildServer.nunit.testReordering.NUnitReorderingTestsSplitService
import jetbrains.buildServer.nunit.testReordering.NUnitXmlTestInfoParser
import jetbrains.buildServer.nunit.testReordering.TestInfo
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class NUnitReorderingWorkflowComposer(
    private val _nUnitXmlTestInfoParser: NUnitXmlTestInfoParser,
    private val _nUnitSettings: NUnitSettings,
    private val _fileSystem: FileSystemService,
    private val _pathsService: PathsService,
    private val _loggerService: LoggerService,
    private val _reorderingTestsSplitService: NUnitReorderingTestsSplitService
) : SimpleWorkflowComposer {
    // NotApplicable because it's explicitly called only in NUnitWorkflowComposer
    override val target = TargetType.NotApplicable

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow): Workflow = sequence {
        val testProjects = mutableListOf<TestProject>()

        // explore
        for (baseCommand in workflow.commandLines) {
            val testListFile = _pathsService.getTempFileName(ALL_TEST_DESC + TESTS_EXT)
            testProjects.add(TestProject(baseCommand, testListFile))
            yield(
                CommandLine(
                    baseCommandLine = null,
                    target = TargetType.AuxiliaryTool,
                    executableFile = baseCommand.executableFile,
                    workingDirectory = baseCommand.workingDirectory,
                    arguments = baseCommand.arguments
                        .plus(CommandLineArgument("--explore=${testListFile.path}"))
                )
            )

        }

        // prepare commands
        val recentlyFailedTests = _nUnitSettings.testReorderingRecentlyFailedTests
        for ((projectIndex, testProject) in testProjects.withIndex()) {
            try {
                publishArtifact(testProject.testListFile, projectIndex)

                val testListStr = _fileSystem.read(testProject.testListFile) { stream ->
                    InputStreamReader(stream).use { return@read it.readText() }
                }

                val testList = _nUnitXmlTestInfoParser.parse(testListStr)
                if (testList.isEmpty()) {
                    _loggerService.writeWarning("Failed to discover tests in project №$projectIndex")
                    continue
                }

                val testsSplit = _reorderingTestsSplitService.splitTests(
                    testList,
                    recentlyFailedTests
                )
                if (testsSplit.firstStepTests.isNotEmpty()) {
                    val firstStepFile = writeTestListToFile(testsSplit.firstStepTests, RECENTLY_FAILED_TEST_DESC)
                    testProject.firstSetup = getCommandLine(testProject.origSetup, firstStepFile)
                    publishArtifact(firstStepFile, projectIndex)
                }

                if (testsSplit.secondStepTests.isNotEmpty()) {
                    val secondStepFile = writeTestListToFile(testsSplit.secondStepTests, OTHERS_TESTS_DESC)
                    testProject.secondSetup = getCommandLine(testProject.origSetup, secondStepFile)
                    publishArtifact(secondStepFile, projectIndex)
                }

            } catch (e: IOException) {
                testProject.firstSetup = null
                testProject.secondSetup = testProject.origSetup

                _loggerService.writeWarning("Failed to split recently failed tests")
                LOG.error(e)
            }
        }

        if (testProjects.isEmpty()) {
            _loggerService.writeBuildProblem(
                "nunit_no_tests_found",
                BuildProblemData.TC_ERROR_MESSAGE_TYPE,
                "Tests are not found"
            )
        }

        // run recently failed tests
        for (testProject in testProjects.filter { it.firstSetup != null }) {
            yield(testProject.firstSetup!!)
        }

        // run rest of tests
        for (testProject in testProjects.filter { it.secondSetup != null }) {
            yield(testProject.secondSetup!!)
        }

        // run failed to discover tests
        for (testProject in testProjects.filter { it.isEmptyProject }) {
            yield(testProject.origSetup)
        }

    }.let(::Workflow)

    private fun getCommandLine(origSetup: CommandLine, testListFile: File): CommandLine = CommandLine(
        baseCommandLine = null,
        target = TargetType.Tool,
        executableFile = origSetup.executableFile,
        workingDirectory = origSetup.workingDirectory,
        arguments = origSetup.arguments
            .plus(CommandLineArgument("--testlist=${testListFile.path}"))
    )

    private fun writeTestListToFile(testList: List<TestInfo>, shortDescription: String): File {
        val testsStr = testList
            .map { it.className }
            .filter { it.isNotEmpty() }
            .toHashSet()
            .joinToString(System.lineSeparator())

        val testListFile = _pathsService.getTempFileName(shortDescription + TESTS_EXT)
        _fileSystem.write(testListFile) { stream -> OutputStreamWriter(stream).use { it.write(testsStr) } }
        return testListFile
    }

    private fun publishArtifact(file: File, index: Int) = _loggerService
        .writeMessage(PublishArtifactsServiceMessage(file.absolutePath, "nUnitStep$index"))

    private inner class TestProject(val origSetup: CommandLine, val testListFile: File) {
        var firstSetup: CommandLine? = null
        var secondSetup: CommandLine? = null

        val isEmptyProject: Boolean
            get() = firstSetup == null && secondSetup == null
    }

    companion object {
        private const val ALL_TEST_DESC = "_all"
        private const val RECENTLY_FAILED_TEST_DESC = "_step1"
        private const val OTHERS_TESTS_DESC = "_step2"
        private const val TESTS_EXT = ".tests"
        private val LOG = Logger.getLogger(NUnitReorderingWorkflowComposer::class.java)
    }
}
