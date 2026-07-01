

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.XmlDocumentServiceImpl
import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.dotnet.discovery.Target
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildProjectDeserializerTest {
    @DataProvider
    fun testDeserializeData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        "/Bank.Test.csproj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Debug"), Configuration("Release")), listOf(Framework("net452")), emptyList(), listOf(Reference("System"), Reference("Microsoft.VisualStudio.QualityTools.UnitTestFramework"), Reference("Microsoft.VisualStudio.QualityTools.CodedUITestFramework")), emptyList(), false, listOf(Property("AssemblyName", "Bank.Test"), Property("TestProjectType", "UnitTest"), Property("OutputType", "Library")))))),
                arrayOf(
                        "/project-runtime.csproj",
                        Solution(listOf(Project("projectPath", emptyList(), emptyList(), listOf(Runtime("win7-x64"), Runtime("win-7x86"), Runtime("ubuntu.16.10-x64")), emptyList(), emptyList(), false, listOf(Property("Sdk", "Microsoft.NET.Sdk")))))),
                arrayOf(
                        "/GeneratePackageOnBuild.csproj",
                        Solution(listOf(Project("projectPath", emptyList(), listOf(Framework("netstandard2.0")), emptyList(), emptyList(), emptyList(), true, listOf(Property("AssemblyName", "Nik.Nuget.Sample"), Property("Sdk", "Microsoft.NET.Sdk")))))),
                arrayOf(
                        "/project14.csproj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Debug"), Configuration("Release")), listOf(Framework("net20")), emptyList(), listOf(Reference("nunit.engine.api"), Reference("System"), Reference("System.Data"), Reference("System.Xml")), emptyList(), false, listOf(Property("AssemblyName", "teamcity-event-listener"), Property("OutputType", "Library")))))),
                arrayOf(
                        "/project.csproj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Core")), listOf(Framework("netcoreapp1.0")), emptyList(), listOf(Reference("Microsoft.NET.Sdk"), Reference("Microsoft.NET.Test.Sdk")), emptyList(), false, listOf(Property("OutputType", "Exe")))))),
                arrayOf(
                        "/build.proj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Release")), emptyList(), emptyList(), emptyList(), listOf(Target("GetNuGet"), Target("Build"), Target("Test")), false, emptyList())))),
                arrayOf(
                        "/project-simplified.csproj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Core")), listOf(Framework("netcoreapp1.0")), emptyList(), listOf(Reference("Microsoft.NET.Sdk"), Reference("Microsoft.NET.Test.Sdk")), emptyList(), false, listOf(Property("OutputType", "Exe"), Property("Sdk", "Microsoft.NET.Sdk")))))),
                arrayOf(
                        "/project-frameworks.csproj",
                        Solution(listOf(Project("projectPath", emptyList(), listOf(Framework("net45"), Framework("netstandard1.3")), emptyList(), listOf(Reference("Newtonsoft.Json")), emptyList(), false, listOf(Property("Sdk", "Microsoft.NET.Sdk")))))))
    }

    @Test(dataProvider = "testDeserializeData")
    fun shouldDeserialize(target: String, expectedSolution: Solution) {
        // Given
        val path = "projectPath"
        val streamFactory = StreamFactoryStub().add(path, this::class.java.getResourceAsStream(target))
        val deserializer = MSBuildProjectDeserializer(XmlDocumentServiceImpl())

        // When
        val actualSolution = deserializer.deserialize(path, streamFactory)

        // Then
        Assert.assertEquals(actualSolution, expectedSolution)
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("abc.proj", true),
                arrayOf("abcPproj", false),
                arrayOf("abc.csproj", true),
                arrayOf("abc.vbproj", true),
                arrayOf("abc3232.vbproj", true),
                arrayOf("abc.Proj", true),
                arrayOf("abc.CSproj", true),
                arrayOf("abc.VBproj", true),
                arrayOf("ab c.VBproj", true),
                arrayOf("dd/ff/abc.VBproj", true),
                arrayOf("c:\\dd\\ff\\abc.VBproj", true),
                arrayOf("abc.sln", false),
                arrayOf("abc.", false),
                arrayOf("abc", false),
                arrayOf("abc.projddd", false),
                arrayOf(".proj", false),
                arrayOf("proj", false),
                arrayOf("csproj", false),
                arrayOf("VBproj", false),
                arrayOf("   ", false),
                arrayOf("", false))
    }

    @Test(dataProvider = "testAcceptData")
    fun shouldAccept(path: String, expectedAccepted: Boolean) {
        // Given
        val deserializer = MSBuildProjectDeserializer(XmlDocumentServiceImpl())

        // When
        val actualAccepted = deserializer.isAccepted(path)

        // Then
        Assert.assertEquals(actualAccepted, expectedAccepted)
    }

    @DataProvider
    fun testMaliciousXmlData(): Array<Array<Any>> {
        val canaryContent = "TW-101822-CANARY-SHOULD-NOT-BE-READ"
        val canaryFile = File.createTempFile("tw-101822-canary", ".txt").apply {
            deleteOnExit()
            writeText(canaryContent)
        }

        return arrayOf(
                arrayOf(
                        "xxe",
                        """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE Project [<!ENTITY xxe SYSTEM "${canaryFile.toURI()}">]>
<Project><PropertyGroup><AssemblyName>&xxe;</AssemblyName></PropertyGroup></Project>""",
                        canaryContent),
                arrayOf(
                        "xml-bomb",
                        """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE Project [
  <!ENTITY a0 "lol">
  <!ENTITY a1 "&a0;&a0;&a0;&a0;&a0;&a0;&a0;&a0;&a0;&a0;">
  <!ENTITY a2 "&a1;&a1;&a1;&a1;&a1;&a1;&a1;&a1;&a1;&a1;">
  <!ENTITY a3 "&a2;&a2;&a2;&a2;&a2;&a2;&a2;&a2;&a2;&a2;">
]>
<Project><PropertyGroup><AssemblyName>&a3;</AssemblyName></PropertyGroup></Project>""",
                        "lollollollol"))
    }

    @Test(dataProvider = "testMaliciousXmlData")
    fun shouldNotResolveMaliciousXml(caseName: String, maliciousXml: String, unsafeContentMarker: String) {
        val path = "projectPath"
        val streamFactory = StreamFactoryStub().add(path, maliciousXml.byteInputStream())
        val deserializer = MSBuildProjectDeserializer(XmlDocumentServiceImpl())

        val solution = try {
            deserializer.deserialize(path, streamFactory)
        } catch (ex: RunBuildException) {
            null // rejected - nothing could have been resolved or expanded
        }

        val resolved = solution?.projects.orEmpty()
                .flatMap { it.properties }
                .any { it.value.contains(unsafeContentMarker) }
        Assert.assertFalse(resolved, "Malicious XML must never be resolved/expanded into discovered build step properties: $solution")
    }
}