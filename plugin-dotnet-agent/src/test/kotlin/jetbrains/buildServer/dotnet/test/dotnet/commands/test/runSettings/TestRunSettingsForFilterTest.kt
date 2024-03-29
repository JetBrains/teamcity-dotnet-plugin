

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.runSettings

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.XmlDocumentServiceImpl
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.build
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.test.runSettings.TestRunSettingsForFilter
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsProvider
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingModeProvider
import org.apache.commons.io.output.WriterOutputStream
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.w3c.dom.Document
import java.io.StringWriter

class TestRunSettingsForFilterTest {
    @MockK private lateinit var _baseTestRunSettingsProvider: TestRunSettingsProvider
    @MockK private lateinit var _testsFilterProvider: TestsFilterProvider
    @MockK private lateinit var _testsSplittingModeProvider: TestsSplittingModeProvider
    @MockK private lateinit var _commandContext: DotnetCommandContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        "aa=bb",
                        XmlDocumentServiceImpl().create(),
                        XmlDocumentServiceImpl().create().build(
                                XmlElement("RunSettings", XmlElement("RunConfiguration", XmlElement("TestCaseFilter", "(aa=bb)")))
                        ).ownerDocument
                ),

                arrayOf(
                        "aa=bb",
                        XmlDocumentServiceImpl().create().build(
                                XmlElement("RunSettings", XmlElement("RunConfiguration", XmlElement("TestCaseFilter", "xx=yy")))
                        ).ownerDocument,
                        XmlDocumentServiceImpl().create().build(
                                XmlElement("RunSettings", XmlElement("RunConfiguration", XmlElement("TestCaseFilter", "(xx=yy)&(aa=bb)")))
                        ).ownerDocument
                ),

                arrayOf(
                        "aa=bb",
                        XmlDocumentServiceImpl().create().build(
                                XmlElement("runSettings", XmlElement("runConfiguration", XmlElement("testCaseFilter", "xx=yy")))
                        ).ownerDocument,
                        XmlDocumentServiceImpl().create().build(
                                XmlElement("runSettings", XmlElement("runConfiguration", XmlElement("testCaseFilter", "(xx=yy)&(aa=bb)")))
                        ).ownerDocument
                ),

                arrayOf(
                        "aa=bb",
                        XmlDocumentServiceImpl().create().build(
                                XmlElement("RunSettings", XmlElement("Abc"), XmlElement("RunConfiguration", XmlElement("Xyz"), XmlElement("TestCaseFilter", "xx=yy")))
                        ).ownerDocument,
                        XmlDocumentServiceImpl().create().build(
                                XmlElement("RunSettings", XmlElement("Abc"), XmlElement("RunConfiguration", XmlElement("Xyz"), XmlElement("TestCaseFilter", "(xx=yy)&(aa=bb)")))
                        ).ownerDocument
                ),

                arrayOf(
                        "aa=bb",
                        XmlDocumentServiceImpl().create().build(
                                XmlElement("OtherRoot", XmlElement("RunConfiguration", XmlElement("TestCaseFilter", "(aa=bb)")))
                        ).ownerDocument,
                        null
                ),
        )
    }

    @Test(dataProvider = "testCases")
    public fun shouldProvideSettingsFile(filterExpression: String, settings: Document?, newExpectedSettings: Document?) {
        // Given
        val provider = createInstance()

        // When
        every { _commandContext.command.commandType } returns DotnetCommandType.Test
        every { _commandContext.toolVersion } returns Version.Empty
        every { _testsSplittingModeProvider.getMode(any()) } returns TestsSplittingMode.TestClassNameFilter
        every { _testsFilterProvider.getFilterExpression(any()) } returns filterExpression
        every { _baseTestRunSettingsProvider.tryCreate(_commandContext) } returns settings
        val newActualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(getString(newActualSettings), getString(newExpectedSettings))
    }

    fun getString(doc: Document?) =
        doc?.let {
            StringWriter().use { writer ->
                WriterOutputStream(writer, Charsets.UTF_8).use { stream ->
                    XmlDocumentServiceImpl().serialize(doc, stream)
                }

                writer.toString()
            }
        }

    private fun createInstance() = TestRunSettingsForFilter(_baseTestRunSettingsProvider, _testsFilterProvider, _testsSplittingModeProvider)
}