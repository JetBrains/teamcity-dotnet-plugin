

package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.find
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE

class MSBuildProjectDeserializer(
        private val _xmlDocumentService: XmlDocumentService) : SolutionDeserializer {
    override fun isAccepted(path: String): Boolean = PathPattern.matcher(path).find()

    override fun deserialize(path: String, streamFactory: StreamFactory): Solution =
            streamFactory.tryCreate(path)?.let {
                it.use {
                    val doc = _xmlDocumentService.deserialize(it)

                    val configurations = getAttributes(doc, "/Project/*[@Condition]", "Condition")
                            .map { ConditionPattern.find(it) }
                            .map { it?.let { it.groupValues[2] } }
                            .filter { !it.isNullOrBlank() }
                            .map { it as String }
                            .plus(getContents(doc, "//PropertyGroup/Configuration"))
                            .distinct()
                            .map { Configuration(it) }
                            .toList()

                    val frameworks = getContents(doc, "//PropertyGroup/TargetFrameworks")
                            .flatMap { it.split(';').asSequence() }
                            .plus(getContents(doc, "/Project/PropertyGroup/TargetFramework"))
                            .plus(getContents(doc, "/Project/PropertyGroup/TargetFrameworkVersion").map { it.replace("v", "net").replace(".", "") })
                            .distinct()
                            .map { Framework(it) }
                            .toList()

                    val runtimes = getContents(doc, "//PropertyGroup/RuntimeIdentifiers")
                            .flatMap { it.split(';').asSequence() }
                            .plus(getContents(doc, "/Project/PropertyGroup/RuntimeIdentifier"))
                            .distinct()
                            .map { Runtime(it) }
                            .toList()

                    val references = getAttributes(doc, "//ItemGroup/PackageReference[@Include]", "Include")
                            .filter { !it.isBlank() }
                            .plus(
                                    getAttributes(doc, "//ItemGroup/Reference[@Include]", "Include")
                                            .map { it.split(',').firstOrNull() }
                                            .filter { !it.isNullOrBlank() })
                            .distinct()
                            .map { Reference(it!!) }
                            .toList()

                    val targets = getAttributes(doc, "/Project/Target[@Name]", "Name")
                            .distinct()
                            .map { Target(it) }
                            .toList()

                    val generatePackageOnBuild = getContents(doc, "/Project/PropertyGroup/GeneratePackageOnBuild")
                            .filter { "true".equals(it.trim(), true) }
                            .any()

                    val properties =
                            getContents(doc, "/Project/PropertyGroup/AssemblyName").map { Property("AssemblyName", it) }
                            .plus(getContents(doc, "/Project/PropertyGroup/TestProjectType").map { Property("TestProjectType", it) })
                            .plus(getContents(doc, "/Project/PropertyGroup/OutputType").map { Property("OutputType", it) })
                            .plus(getAttributes(doc, "/Project", "Sdk").map { Property("Sdk", it) })
                            .toList()

                    Solution(listOf(Project(path, configurations, frameworks, runtimes, references, targets, generatePackageOnBuild, properties)))
                }
            } ?: Solution(emptyList())

    private fun getContents(doc: Document, xpath: String): Sequence<String> =
            doc.find<Element>(xpath).map { it.textContent }.filter { !it.isNullOrBlank() }

    private fun getAttributes(doc: Document, xpath: String, attributeName: String): Sequence<String> =
            doc.find<Element>(xpath).map { it.getAttribute(attributeName) }.filter { !it.isNullOrBlank() }

    companion object {
        private val ConditionPattern: Regex = Regex("'\\$\\(Configuration\\)([^']*)' == '([^|]*)([^']*)'", RegexOption.IGNORE_CASE)
        private val PathPattern: Pattern = Pattern.compile("^.+\\.(proj|csproj|vbproj)$", CASE_INSENSITIVE)
    }
}