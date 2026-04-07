package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.Serializer
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.XmlDocumentServiceImpl
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotcover.CoverageFilter
import jetbrains.buildServer.dotcover.CoverageFilterProvider
import jetbrains.buildServer.dotcover.DotCoverProject
import jetbrains.buildServer.dotcover.DotCoverProject.*
import jetbrains.buildServer.dotcover.DotCoverRunConfigFileSerializerImpl
import jetbrains.buildServer.dotcover.command.DotCoverCommandType
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Invocation
import org.jmock.lib.action.CustomAction
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.w3c.dom.Document
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

class DotCoverRunConfigFileSerializerTest {
    private val _realXmlDocumentService: XmlDocumentService = XmlDocumentServiceImpl()
    private val _argumentsService: ArgumentsService = ArgumentsServiceStub()
    private lateinit var _ctx: Mockery
    private lateinit var _pathService: PathsService
    private lateinit var _xmlDocumentService: XmlDocumentService
    private lateinit var _coverageFilterProvider: CoverageFilterProvider

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx.mock<PathsService>(PathsService::class.java)
        _xmlDocumentService = _ctx.mock<XmlDocumentService>(XmlDocumentService::class.java)
        _coverageFilterProvider = _ctx.mock<CoverageFilterProvider>(CoverageFilterProvider::class.java)
    }

    @Test
    fun shouldGenerateCoverCommandContent() {
        // Arrange
        val outputStream = ByteArrayOutputStream()
        val document = _realXmlDocumentService.create()
        val tempDir = File("temp")
        val workingDirectory = Path(File("workDir").path)
        val tool = Path(File("wd", "tool").path)
        val expectedContent = "<CoverageParams xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<Executable>" + tool.path + "</Executable>" +
                "<Arguments>arg1</Arguments>" +
                "<WorkingDir>" + workingDirectory.path + "</WorkingDir>" +
                "<Output>" + File(tempDir, "snapshot.dcvr").path + "</Output>" +
                "<Filters>" +
                "<IncludeFilters>" +
                "<FilterEntry>" +
                "<ModuleMask>aaa</ModuleMask>" +
                "<ClassMask>*</ClassMask>" +
                "<FunctionMask>*</FunctionMask>" +
                "</FilterEntry>" +
                "</IncludeFilters>" +
                "<ExcludeFilters>" +
                "<FilterEntry>" +
                "<ModuleMask>bbb</ModuleMask>" +
                "<ClassMask>*</ClassMask>" +
                "<FunctionMask>*</FunctionMask>" +
                "</FilterEntry>" +
                "</ExcludeFilters>" +
                "</Filters>" +
                "<AttributeFilters>" +
                "<AttributeFilterEntry>" +
                "<ClassMask>aaa</ClassMask>" +
                "</AttributeFilterEntry>" +
                "<AttributeFilterEntry>" +
                "<ClassMask>bbb</ClassMask>" +
                "</AttributeFilterEntry>" +
                "</AttributeFilters>" +
                "</CoverageParams>"

        _ctx.checking(object : Expectations() {
            init {
                oneOf<XmlDocumentService>(_xmlDocumentService).create()
                will(returnValue(document))

                oneOf<CoverageFilterProvider>(_coverageFilterProvider).filters
                will(returnValue(sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, "aaa", CoverageFilter.Any, CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "bbb", CoverageFilter.Any, CoverageFilter.Any)
                )))

                oneOf<CoverageFilterProvider>(_coverageFilterProvider).attributeFilters
                will(returnValue(sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "aaa", CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "bbb", CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, CoverageFilter.Any, "ccc", CoverageFilter.Any)
                )))

                oneOf<XmlDocumentService>(_xmlDocumentService).serialize(document, outputStream)
                will(object : CustomAction("doc") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any? {
                        _realXmlDocumentService.serialize(invocation.getParameter(0) as Document, invocation.getParameter(1) as OutputStream)
                        return null
                    }
                })
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
        val actual = String(outputStream.toByteArray()).trimXml()
        val expected = expectedContent.trimXml()

        // Assert
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun shouldGenerateMergeCommandContent() {
        // Arrange
        val outputStream = ByteArrayOutputStream()
        val document = _realXmlDocumentService.create()
        val tempDir = File("temp")
        val expectedContent = """
                <MergeParams xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <Source>${File(tempDir, "1.dcvr").absolutePath}</Source>
                <Source>${File(tempDir, "2.dcvr").absolutePath}</Source>
                <Source>${File(tempDir, "3.dcvr").absolutePath}</Source>
                <Output>${File(tempDir, "outputSnapshot_BuildStep1.dcvr").absolutePath}</Output>
                </MergeParams>
                """

        _ctx.checking(object : Expectations() {
            init {
                oneOf<XmlDocumentService>(_xmlDocumentService).create()
                will(returnValue(document))
                oneOf<XmlDocumentService>(_xmlDocumentService).serialize(document, outputStream)
                will(object : CustomAction("doc") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any? {
                        _realXmlDocumentService.serialize(invocation.getParameter(0) as Document, invocation.getParameter(1) as OutputStream)
                        return null
                    }
                })
            }
        })

        val instance = createInstance()
        val dotCoverProject = DotCoverProject(
            DotCoverCommandType.Merge,
            mergeCommandData = MergeCommandData(
                listOf(
                    Path(File(tempDir, "1.dcvr").absolutePath),
                    Path(File(tempDir, "2.dcvr").absolutePath),
                    Path(File(tempDir, "3.dcvr").absolutePath)
                ),
                Path(File(tempDir, "outputSnapshot_BuildStep1.dcvr").absolutePath)
            )
        )

        // Act
        instance.serialize(dotCoverProject, outputStream)
        val actual = String(outputStream.toByteArray()).trimXml()
        val expected = expectedContent.trimXml()

        // Assert
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun shouldGenerateReportCommandContent() {
        // Arrange
        val outputStream = ByteArrayOutputStream()
        val document = _realXmlDocumentService.create()
        val tempDir = File("temp")
        val expectedContent = """
                <ReportParams xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <Source>${File(tempDir,"outputSnapshot_BuildStep1.dcvr").absolutePath}</Source>
                <Output>${File(tempDir, "CoverageReport_BuildStep1.xml").absolutePath}</Output>
                <ReportType>TeamCityXML</ReportType>
                </ReportParams>
                """

        _ctx.checking(object : Expectations() {
            init {
                oneOf<XmlDocumentService>(_xmlDocumentService).create()
                will(returnValue(document))
                oneOf<XmlDocumentService>(_xmlDocumentService).serialize(document, outputStream)
                will(object : CustomAction("doc") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any? {
                        _realXmlDocumentService.serialize(invocation.getParameter(0) as Document, invocation.getParameter(1) as OutputStream)
                        return null
                    }
                })
            }
        })

        val instance = createInstance()
        val dotCoverProject = DotCoverProject(
            DotCoverCommandType.Report,
            reportCommandData = ReportCommandData(
                Path(File(tempDir, "outputSnapshot_BuildStep1.dcvr").absolutePath),
                Path(File(tempDir, "CoverageReport_BuildStep1.xml").absolutePath)
            )
        )

        // Act
        instance.serialize(dotCoverProject, outputStream)
        val actual = String(outputStream.toByteArray()).trimXml()
        val expected = expectedContent.trimXml()

        // Assert
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun shouldGenerateContentWhenNoFilters() {
        // Arrange
        val outputStream = ByteArrayOutputStream()
        val document = _realXmlDocumentService.create()
        val tempDir = File("temp")
        val workingDirectory = Path("workDir")
        val tool = Path(File("wd", "tool").path)
        val expectedContent = "<CoverageParams xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<Executable>" + tool.path + "</Executable>" +
                "<Arguments/>" +
                "<WorkingDir>" + workingDirectory.path + "</WorkingDir>" +
                "<Output>" + File(tempDir, "snapshot.dcvr").path + "</Output>" +
                "</CoverageParams>"

        _ctx.checking(object : Expectations() {
            init {
                oneOf<XmlDocumentService>(_xmlDocumentService).create()
                will(returnValue(document))

                oneOf<CoverageFilterProvider>(_coverageFilterProvider).filters
                will(returnValue(emptySequence<CoverageFilter>()))

                oneOf<CoverageFilterProvider>(_coverageFilterProvider).attributeFilters
                will(returnValue(emptySequence<CoverageFilter>()))

                oneOf<XmlDocumentService>(_xmlDocumentService).serialize(document, outputStream)
                will(object : CustomAction("doc") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any? {
                        _realXmlDocumentService.serialize(invocation.getParameter(0) as Document, invocation.getParameter(1) as OutputStream)
                        return null
                    }
                })
            }
        })

        val instance = createInstance()
        val dotCoverProject = DotCoverProject(
            DotCoverCommandType.Cover,
            CoverCommandData(
                CommandLine(null, TargetType.Tool, tool, workingDirectory, emptyList(), emptyList()),
                Path(File(tempDir, "config.dotCover").path),
                Path(File(tempDir, "snapshot.dcvr").path)))

        // Act
        instance.serialize(dotCoverProject, outputStream)

        val actual = String(outputStream.toByteArray()).trimXml()
        val expected = expectedContent.trimXml()

        // Assert
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actual, expected)
    }

    private fun createInstance(): Serializer<DotCoverProject> {
        return DotCoverRunConfigFileSerializerImpl(
                _xmlDocumentService,
                _argumentsService,
                _coverageFilterProvider
        )
    }

    private fun String.trimXml(): String {
        return this.trim { it <= ' ' }.replace("\n", "").replace("\r", "").replace(" ", "")
    }
}