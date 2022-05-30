package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.E
import jetbrains.buildServer.XmlDocumentServiceImpl
import jetbrains.buildServer.build
import jetbrains.buildServer.dotnet.*
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
                                E("RunSettings", E("RunConfiguration", E("TestCaseFilter", "(aa=bb)")))
                        ).ownerDocument
                ),

                arrayOf(
                        "aa=bb",
                        XmlDocumentServiceImpl().create().build(
                                E("RunSettings", E("RunConfiguration", E("TestCaseFilter", "xx=yy")))
                        ).ownerDocument,
                        XmlDocumentServiceImpl().create().build(
                                E("RunSettings", E("RunConfiguration", E("TestCaseFilter", "(xx=yy)&(aa=bb)")))
                        ).ownerDocument
                ),

                arrayOf(
                        "aa=bb",
                        XmlDocumentServiceImpl().create().build(
                                E("runSettings", E("runConfiguration", E("testCaseFilter", "xx=yy")))
                        ).ownerDocument,
                        XmlDocumentServiceImpl().create().build(
                                E("runSettings", E("runConfiguration", E("testCaseFilter", "(xx=yy)&(aa=bb)")))
                        ).ownerDocument
                ),

                arrayOf(
                        "aa=bb",
                        XmlDocumentServiceImpl().create().build(
                                E("RunSettings", E("Abc"), E("RunConfiguration", E("Xyz"), E("TestCaseFilter", "xx=yy")))
                        ).ownerDocument,
                        XmlDocumentServiceImpl().create().build(
                                E("RunSettings", E("Abc"), E("RunConfiguration", E("Xyz"), E("TestCaseFilter", "(xx=yy)&(aa=bb)")))
                        ).ownerDocument
                ),

                arrayOf(
                        "aa=bb",
                        XmlDocumentServiceImpl().create().build(
                                E("OtherRoot", E("RunConfiguration", E("TestCaseFilter", "(aa=bb)")))
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
        every { _testsFilterProvider.filterExpression } returns filterExpression
        every { _baseTestRunSettingsProvider.tryCreate(DotnetCommandType.Test) } returns settings
        val newActualSettings = provider.tryCreate(DotnetCommandType.Test)

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

    private fun createInstance() = TestRunSettingsForFilter(_baseTestRunSettingsProvider, _testsFilterProvider)
}