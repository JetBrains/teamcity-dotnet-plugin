package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.E
import jetbrains.buildServer.agent.DataProcessorContext
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.duplicates.DuplicatesReporter
import jetbrains.buildServer.agent.inspections.InspectionInstance
import jetbrains.buildServer.agent.inspections.InspectionReporter
import jetbrains.buildServer.agent.inspections.InspectionTypeInfo
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.duplicator.DuplicateInfo
import jetbrains.buildServer.inspect.DupFinderCodeDataProcessor
import jetbrains.buildServer.inspect.InspectCodeDataProcessor
import jetbrains.buildServer.inspect.XmlReader
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.io.PipedInputStream

class DupFinderCodeDataProcessorTest {
    @MockK private lateinit var _xmlReader: XmlReader
    @MockK private lateinit var _reporter: DuplicatesReporter
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

        every { _reporter.startDuplicates() } returns Unit
        every { _reporter.finishDuplicates() } returns Unit
    }

    @Test
    fun shouldProcessData() {
        // Given
        val elements = sequenceOf(
                E("DuplicatesReport"),
                E("Statistics"),
                E("CodebaseCost", "611792"),
                E("Duplicates"),
                E("Duplicate")
                        .a("Cost", "10"),
                E("Fragment"),
                E("FileName", "IoC.Source\\IoC.cs"),
                E("OffsetRange")
                        .a("Start", "123")
                        .a("End", "345"),
                E("LineRange")
                        .a("Start", "1")
                        .a("End", "5"),
                E("Text", "Some text 1\nLine 2"),
                E("Fragment"),
                E("FileName", "IoC.Source\\IoC2.cs"),
                E("OffsetRange")
                        .a("Start", "123")
                        .a("End", "345"),
                E("LineRange")
                        .a("Start", "2")
                        .a("End", "6"),
                E("Text", "Some text 2\nLine 2"),
                E("Duplicate")
                        .a("Cost", "20"),
                E("Fragment"),
                E("FileName", "IoC.Source3\\IoC.cs"),
                E("OffsetRange")
                        .a("Start", "123")
                        .a("End", "345"),
                E("LineRange")
                        .a("Start", "3")
                        .a("End", "7"),
                E("Text", "Some text 3\nLine 2")
        )

        var dataFile = File("DataFile.xml")
        val inputStream = PipedInputStream()
        val fileSystem = VirtualFileSystemService().addFile(dataFile, VirtualFileSystemService.Attributes(), inputStream)
        val actualDuplicates = mutableListOf<DuplicateInfo>()

        val processor = createInstance(fileSystem)
        every { _context.file }.returns(dataFile)
        every { _xmlReader.read(inputStream) } returns elements
        every { _reporter.addDuplicates(any()) } answers {
            actualDuplicates.addAll(arg<Collection<DuplicateInfo>>(0))
        }

        // When
        processor.processData(_context)

        // Then
        verify { _reporter.startDuplicates() }
        verify { _reporter.finishDuplicates() }
        Assert.assertEquals(actualDuplicates.size, 2)

        Assert.assertEquals(actualDuplicates[0].cost, 10)
        Assert.assertEquals(actualDuplicates[0].hash, -1079548696)
        Assert.assertEquals(actualDuplicates[0].fragments.size, 2)

        Assert.assertEquals(actualDuplicates[0].fragments[0].file, "IoC.Source/IoC.cs")
        Assert.assertEquals(actualDuplicates[0].fragments[0].startLine, 1)
        Assert.assertEquals(actualDuplicates[0].fragments[0].offsetInfo, "line:1,5")
        Assert.assertEquals(actualDuplicates[0].fragments[0].hash, 437592128)

        Assert.assertEquals(actualDuplicates[0].fragments[1].file, "IoC.Source/IoC2.cs")
        Assert.assertEquals(actualDuplicates[0].fragments[1].startLine, 2)
        Assert.assertEquals(actualDuplicates[0].fragments[1].offsetInfo, "line:2,6")
        Assert.assertEquals(actualDuplicates[0].fragments[1].hash, -1801836637)

        Assert.assertEquals(actualDuplicates[1].cost, 20)
        Assert.assertEquals(actualDuplicates[1].hash, 1479550781)
        Assert.assertEquals(actualDuplicates[1].fragments.size, 1)

        Assert.assertEquals(actualDuplicates[1].fragments[0].file, "IoC.Source3/IoC.cs")
        Assert.assertEquals(actualDuplicates[1].fragments[0].startLine, 3)
        Assert.assertEquals(actualDuplicates[1].fragments[0].offsetInfo, "line:3,7")
        Assert.assertEquals(actualDuplicates[1].fragments[0].hash, 1479550782)
    }

    private fun createInstance(fileSystem: FileSystemService) =
            DupFinderCodeDataProcessor(
                    fileSystem,
                    _xmlReader,
                    _reporter)
}