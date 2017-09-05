package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotcover.CoverageFilter
import jetbrains.buildServer.dotcover.CoverageFilterProvider
import jetbrains.buildServer.dotcover.DotCoverProject
import jetbrains.buildServer.dotcover.DotCoverProjectSerializerImpl
import jetbrains.buildServer.runners.*
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

class DotCoverProjectSerializerTest {
    private val _realXmlDocumentService: XmlDocumentService = XmlDocumentServiceImpl()
    private val _argumentsService: ArgumentsService = ArgumentsServiceStub()
    private var _ctx: Mockery? = null
    private var _pathService: PathsService? = null
    private var _xmlDocumentService: XmlDocumentService? = null
    private var _coverageFilterProvider: CoverageFilterProvider? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx!!.mock<PathsService>(PathsService::class.java)
        _xmlDocumentService = _ctx!!.mock<XmlDocumentService>(XmlDocumentService::class.java)
        _coverageFilterProvider = _ctx!!.mock<CoverageFilterProvider>(CoverageFilterProvider::class.java)
    }

    @Test
    fun shouldGenerateContent() {
        // Given
        val outputStream = ByteArrayOutputStream()
        val document = _realXmlDocumentService.create()
        val tempDir = File("temp")
        val workingDirectory = File("workDir")
        val tool = File("wd", "tool")
        val expectedContent = "<CoverageParams xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<Executable>" + tool.absolutePath + "</Executable>" +
                "<Arguments>arg1</Arguments>" +
                "<WorkingDir>" + workingDirectory.absolutePath + "</WorkingDir>" +
                "<Output>" + File(tempDir, "snapshot.dcvr").absolutePath + "</Output>" +
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

        _ctx!!.checking(object : Expectations() {
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
            CommandLine(TargetType.Tool, tool, workingDirectory, listOf(CommandLineArgument("arg1")), emptyList()),
            File(tempDir, "config.dotCover"),
            File(tempDir, "snapshot.dcvr"))

        // When
        instance.serialize(dotCoverProject, outputStream)
        val actual = String(outputStream.toByteArray()).trim({ it <= ' ' }).replace("\n", "").replace("\r", "")
        val expected = expectedContent.trim { it <= ' ' }.replace("\n", "").replace("\r", "")

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actual, expected);
    }

    @Test
    fun shouldGenerateContentWhenNoFilters() {
        // Given
        val outputStream = ByteArrayOutputStream()
        val document = _realXmlDocumentService.create()
        val tempDir = File("temp")
        val workingDirectory = File("workDir")
        val tool = File("wd", "tool")
        val expectedContent = "<CoverageParams xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<Executable>" + tool.absolutePath + "</Executable>" +
                "<Arguments/>" +
                "<WorkingDir>" + workingDirectory.absolutePath + "</WorkingDir>" +
                "<Output>" + File(tempDir, "snapshot.dcvr").absolutePath + "</Output>" +
                "</CoverageParams>"

        _ctx!!.checking(object : Expectations() {
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
            CommandLine(TargetType.Tool, tool, workingDirectory, emptyList(), emptyList()),
            File(tempDir, "config.dotCover"),
            File(tempDir, "snapshot.dcvr"))

        // When
        instance.serialize(dotCoverProject, outputStream)

        val actual = String(outputStream.toByteArray()).trim({ it <= ' ' }).replace("\n", "").replace("\r", "")
        val expected = expectedContent.trim { it <= ' ' }.replace("\n", "").replace("\r", "")

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actual, expected);
    }

    private fun createInstance(): Serializer<DotCoverProject> {
        return DotCoverProjectSerializerImpl(
                _pathService!!,
                _xmlDocumentService!!,
                _argumentsService,
                _coverageFilterProvider!!)
    }

    private class ArgumentsServiceStub: ArgumentsService {
        override fun escape(text: String): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun combine(arguments: Sequence<String>): String = arguments.joinToString(" ")

        override fun split(text: String): Sequence<String> = TODO("not implemented")
    }
}