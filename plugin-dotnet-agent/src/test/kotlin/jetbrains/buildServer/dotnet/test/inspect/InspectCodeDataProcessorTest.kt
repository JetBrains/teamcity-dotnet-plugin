package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.E
import jetbrains.buildServer.agent.DataProcessorContext
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.inspections.*
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.inspect.InspectCodeDataProcessor
import jetbrains.buildServer.inspect.XmlReader
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.io.PipedInputStream

class InspectCodeDataProcessorTest {
    @MockK private lateinit var _xmlReader: XmlReader
    @MockK private lateinit var _reporter: InspectionReporter
    @MockK private lateinit var _context: DataProcessorContext

    private var _csharpErrors: InspectionTypeInfo
    private var _redundantUsingDirective: InspectionTypeInfo
    private var _unusedTypeGlobal: InspectionTypeInfo

    private var _redundantUsingDirectiveIssue: InspectionInstance
    private var _csharpErrorsIssue: InspectionInstance
    private var _unusedTypeGlobalIssue: InspectionInstance
    private var _csharpErrorsIssue2: InspectionInstance

    init {
        _csharpErrors = InspectionTypeInfo()
        _csharpErrors.id = "CSharpErrors"
        _csharpErrors.name = null
        _csharpErrors.category = "C# Compiler Errors"
        _csharpErrors.description = ""

        _redundantUsingDirective = InspectionTypeInfo()
        _redundantUsingDirective.id = "RedundantUsingDirective"
        _redundantUsingDirective.name = "Redundant using directive"
        _redundantUsingDirective.category = "Redundancies in Code"
        _redundantUsingDirective.description = "https://www.jetbrains.com/resharperplatform/help?Keyword=RedundantUsingDirective"

        _unusedTypeGlobal = InspectionTypeInfo()
        _unusedTypeGlobal.id = "UnusedType.Global"
        _unusedTypeGlobal.name = "Type is never used: Non-private accessibility"
        _unusedTypeGlobal.category = "Redundancies in Symbol Declarations"
        _unusedTypeGlobal.description = "https://www.jetbrains.com/resharperplatform/help?Keyword=UnusedType.Global"

        _redundantUsingDirectiveIssue = InspectionInstance()
        _redundantUsingDirectiveIssue.inspectionId = "RedundantUsingDirective"
        _redundantUsingDirectiveIssue.filePath = "Clock.Console/Program.cs"
        _redundantUsingDirectiveIssue.line = 5;
        _redundantUsingDirectiveIssue.message = "Using directive is not required by the code and can be safely removed"
        _redundantUsingDirectiveIssue.addAttribute("SEVERITY", listOf("WARNING"))

        _csharpErrorsIssue = InspectionInstance()
        _csharpErrorsIssue.inspectionId = "CSharpErrors"
        _csharpErrorsIssue.filePath = "Clock.Console/Program.cs"
        _csharpErrorsIssue.line = 7;
        _csharpErrorsIssue.message = "Cannot resolve symbol 'IoC'"
        _csharpErrorsIssue.addAttribute("SEVERITY", listOf("ERROR"))

        _unusedTypeGlobalIssue = InspectionInstance()
        _unusedTypeGlobalIssue.inspectionId = "UnusedType.Global"
        _unusedTypeGlobalIssue.filePath = "Clock.Console/Program.cs"
        _unusedTypeGlobalIssue.line = 9;
        _unusedTypeGlobalIssue.message = "Abc"
        _unusedTypeGlobalIssue.addAttribute("SEVERITY", listOf("INFO"))

        _csharpErrorsIssue2 = InspectionInstance()
        _csharpErrorsIssue2.inspectionId = "CSharpErrors"
        _csharpErrorsIssue2.filePath = "Clock.Xamarin.Android/MainActivity.cs"
        _csharpErrorsIssue2.line = 28;
        _csharpErrorsIssue2.message = "Cannot resolve symbol 'OnRequestPermissionsResult'"
        _csharpErrorsIssue2.addAttribute("SEVERITY", listOf("ERROR"))
    }

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _reporter.markBuildAsInspectionsBuild() } returns Unit
        every { _reporter.reportInspectionType(any()) } returns Unit
        every { _reporter.reportInspection(any()) } returns Unit
    }

    @DataProvider(name = "processDataCases")
    fun getProcessDataCases(): Array<Array<Sequence<Any>>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(
                                E("Report").a("ToolsVersion", "203"),
                                E("Information"),
                                E("InspectionScope"),
                                E("Element"),
                                E("IssueTypes"),
                                E("IssueType")
                                        .a("Id", "CSharpErrors")
                                        .a("Category", "C# Compiler Errors")
                                        .a("CategoryId", "CSharpErrors")
                                        .a("Severity", "ERROR"),
                                E("IssueType")
                                        .a("Id", "RedundantUsingDirective")
                                        .a("Category", "Redundancies in Code")
                                        .a("CategoryId", "CodeRedundancy")
                                        .a("Description", "Redundant using directive")
                                        .a("Severity", "WARNING")
                                        .a("WikiUrl", "https://www.jetbrains.com/resharperplatform/help?Keyword=RedundantUsingDirective"),
                                E("IssueType")
                                        .a("Id", "UnusedType.Global")
                                        .a("Category", "Redundancies in Symbol Declarations")
                                        .a("CategoryId", "DeclarationRedundancy")
                                        .a("Description", "Type is never used: Non-private accessibility")
                                        .a("Severity", "SUGGESTION")
                                        .a("WikiUrl", "https://www.jetbrains.com/resharperplatform/help?Keyword=UnusedType.Global"),
                                E("Issues"),
                                E("Project").a("Name", "Clock.Console"),
                                E("Issue")
                                        .a("TypeId", "RedundantUsingDirective")
                                        .a("File", "Clock.Console\\Program.cs")
                                        .a("Offset", "85-103")
                                        .a("Line", "5")
                                        .a("Message", "Using directive is not required by the code and can be safely removed"),
                                E("Issue")
                                        .a("TypeId", "CSharpErrors")
                                        .a("File", "Clock.Console\\Program.cs")
                                        .a("Offset", "99-102")
                                        .a("Line", "7")
                                        .a("Message", "Cannot resolve symbol 'IoC'"),
                                E("Issue")
                                        .a("TypeId", "UnusedType.Global")
                                        .a("File", "Clock.Console\\Program.cs")
                                        .a("Offset", "99-102")
                                        .a("Line", "9")
                                        .a("Message", "Abc"),
                                E("Project").a("Name", "Clock.Xamarin.Android"),
                                E("Issue")
                                        .a("TypeId", "CSharpErrors")
                                        .a("File", "Clock.Xamarin.Android\\MainActivity.cs")
                                        .a("Offset", "1266-1292")
                                        .a("Line", "28")
                                        .a("Message", "Cannot resolve symbol 'OnRequestPermissionsResult'")
                        ),
                        sequenceOf(_csharpErrors, _redundantUsingDirective, _unusedTypeGlobal),
                        sequenceOf(_redundantUsingDirectiveIssue, _csharpErrorsIssue, _unusedTypeGlobalIssue, _csharpErrorsIssue2)
                )
        )
    }

    @Test(dataProvider = "processDataCases")
    fun shouldProcessData(
            elements: Sequence<E>,
            expectedTypes: Sequence<InspectionTypeInfo>,
            expectedInstances: Sequence<InspectionInstance>) {
        // Given
        var dataFile = File("DataFile.xml")
        val inputStream = PipedInputStream()
        val fileSystem = VirtualFileSystemService().addFile(dataFile, VirtualFileSystemService.Attributes(), inputStream)

        val processor = createInstance(fileSystem)
        every { _context.file }.returns(dataFile)
        every { _xmlReader.read(inputStream) } returns elements

        // When
        processor.processData(_context)

        // Then
        verify { _reporter.markBuildAsInspectionsBuild() }
        for (expectedType in expectedTypes) {
            verify { _reporter.reportInspectionType(match { expectedType.toString() == it.toString() }) }
        }

        for (expectedInstance in expectedInstances) {
            verify { _reporter.reportInspection(match { expectedInstance.toString() == it.toString() }) }
        }
    }

    private fun createInstance(fileSystem: FileSystemService) =
            InspectCodeDataProcessor(
                    fileSystem,
                    _xmlReader,
                    _reporter)
}