package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.Serializer
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotcover.CoverageFilter
import jetbrains.buildServer.dotcover.CoverageFilterProvider
import jetbrains.buildServer.dotcover.DotCoverProject
import jetbrains.buildServer.dotcover.DotCoverProject.CoverCommandData
import jetbrains.buildServer.dotcover.DotCoverResponseFileSerializerImpl
import jetbrains.buildServer.dotcover.command.DotCoverCommandType
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.rx.assertEquals
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.File

class DotCoverResponseFileSerializerTest {
    private val _argumentsService: ArgumentsService = ArgumentsServiceStub()
    private lateinit var _ctx: Mockery
    private lateinit var _coverageFilterProvider: CoverageFilterProvider
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _loggerService = _ctx.mock<LoggerService>(LoggerService::class.java)
        _coverageFilterProvider = _ctx.mock<CoverageFilterProvider>(CoverageFilterProvider::class.java)
    }

    @Test
    fun `should generate cover command content`() {
        // Arrange
        val outputStream = ByteArrayOutputStream()
        val tempDir = File("temp")
        val workingDirectory = Path(File("wd").path)
        val tool = Path(File("wd", "tool").path)
        val expectedContent = """
            --target-executable
            ${tool.path}
            --target-working-directory
            ${workingDirectory.path}
            --snapshot-output
            ${File(tempDir, "snapshot.dcvr").path}
            --exclude-assemblies
            fb
            --exclude-attributes
            afa,afb
            --
            arg1
        """.trimIndent()

        _ctx.checking(object : Expectations() {
            init {
                oneOf<CoverageFilterProvider>(_coverageFilterProvider).filters
                will(returnValue(sequenceOf(
                    CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, "fa", CoverageFilter.Any, CoverageFilter.Any),
                    CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "fb", CoverageFilter.Any, CoverageFilter.Any)
                )))

                oneOf<CoverageFilterProvider>(_coverageFilterProvider).attributeFilters
                will(returnValue(sequenceOf(
                    CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "afa", CoverageFilter.Any),
                    CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "afb", CoverageFilter.Any),
                )))

                // include filter "fa" will trigger a warning
                // JMock matcher returns default (null) value for String, and Kotlin doesn't like that, so have a dummy elvis operator fallback here
                allowing<LoggerService>(_loggerService).writeWarning(with(anything<String>()) ?: "Glitter in the sky, glitter in my eyes")
            }
        })

        val instance = createInstance()
        val dotCoverProject = DotCoverProject(
            DotCoverCommandType.Cover,
            CoverCommandData(
                CommandLine(null, TargetType.Tool, tool, workingDirectory, listOf(CommandLineArgument("arg1")), emptyList()),
                Path(File(tempDir, "config.dotCover").path),
                Path(File(tempDir, "snapshot.dcvr").path)
            )
        )

        // Act
        instance.serialize(dotCoverProject, outputStream)
        val actual = String(outputStream.toByteArray()).trim()
        val expected = expectedContent.trim()

        // Assert
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun `should generate merge command content`() {
        val outputStream = ByteArrayOutputStream()
        val tempDir = File("temp")
        val expectedContent = """
          --snapshot-source
          ${File(tempDir, "1.dcvr").absolutePath},${File(tempDir, "2.dcvr").absolutePath},${File(tempDir, "3.dcvr").absolutePath}
          --snapshot-output
          ${File(tempDir, "outputSnapshot_BuildStep1.dcvr").absolutePath}
      """.trimIndent()

        val instance = createInstance()
        val dotCoverProject = DotCoverProject(
            DotCoverCommandType.Merge,
            mergeCommandData = DotCoverProject.MergeCommandData(
                listOf(
                    Path(File(tempDir, "1.dcvr").absolutePath),
                    Path(File(tempDir, "2.dcvr").absolutePath),
                    Path(File(tempDir, "3.dcvr").absolutePath)
                ),
                Path(File(tempDir, "outputSnapshot_BuildStep1.dcvr").absolutePath)
            )
        )

        instance.serialize(dotCoverProject, outputStream)
        val actual = String(outputStream.toByteArray()).trim()

        Assert.assertEquals(actual, expectedContent)
    }

    @Test
    fun `should generate report command content`() {
        val outputStream = ByteArrayOutputStream()
        val tempDir = File("temp")
        val expectedContent = """
            --snapshot-source
            ${File(tempDir, "outputSnapshot_BuildStep1.dcvr").absolutePath}
            --xml-report-output
            ${File(tempDir, "CoverageReport_BuildStep1.xml").absolutePath}
        """.trimIndent()

        val instance = createInstance()
        val dotCoverProject = DotCoverProject(
            DotCoverCommandType.Report,
            reportCommandData = DotCoverProject.ReportCommandData(
                Path(File(tempDir, "outputSnapshot_BuildStep1.dcvr").absolutePath),
                Path(File(tempDir, "CoverageReport_BuildStep1.xml").absolutePath)
            )
        )

        instance.serialize(dotCoverProject, outputStream)
        val actual = String(outputStream.toByteArray()).trim()

        Assert.assertEquals(actual, expectedContent)
    }

    @Test
    fun `should generate cover command content when no filters`() {
        val outputStream = ByteArrayOutputStream()
        val tempDir = File("temp")
        val workingDirectory = Path("workDir")
        val tool = Path(File("wd", "tool").path)
        val expectedContent = """
            --target-executable
            ${tool.path}
            --target-working-directory
            ${workingDirectory.path}
            --snapshot-output
            ${File(tempDir, "snapshot.dcvr").path}
        """.trimIndent()

        _ctx.checking(object : Expectations() {
            init {
                oneOf<CoverageFilterProvider>(_coverageFilterProvider).filters
                will(returnValue(emptySequence<CoverageFilter>()))

                oneOf<CoverageFilterProvider>(_coverageFilterProvider).attributeFilters
                will(returnValue(emptySequence<CoverageFilter>()))
            }
        })

        val instance = createInstance()
        val dotCoverProject = DotCoverProject(
            DotCoverCommandType.Cover,
            CoverCommandData(
                CommandLine(null, TargetType.Tool, tool, workingDirectory, emptyList(), emptyList()),
                Path(File(tempDir, "config.dotCover").path),
                Path(File(tempDir, "snapshot.dcvr").path)
            )
        )

        instance.serialize(dotCoverProject, outputStream)
        val actual = String(outputStream.toByteArray()).trim()

        _ctx.assertIsSatisfied()
        Assert.assertEquals(actual, expectedContent.trim())
    }

    private fun createInstance(): Serializer<DotCoverProject> {
        return DotCoverResponseFileSerializerImpl(
            _argumentsService,
            _coverageFilterProvider,
            _loggerService
        )
    }
}