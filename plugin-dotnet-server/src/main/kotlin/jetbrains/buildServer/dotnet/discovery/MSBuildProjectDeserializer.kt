@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.XmlDocumentService
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.coroutines.experimental.buildSequence

class MSBuildProjectDeserializer(
        private val _xmlDocumentService: XmlDocumentService) : SolutionDeserializer {
    override fun accept(path: String): Boolean = PathPattern.matcher(path).find()

    override fun deserialize(path: String, streamFactory: StreamFactory): Solution =
            streamFactory.tryCreate(path)?.let {
                it.use {
                    val doc = _xmlDocumentService.deserialize(it)

                    val configurations = getAttributes(doc, "/Project/*[@Condition]", "Condition")
                            .map { ConditionPattern.find(it) }
                            .map { it?.let { it.groupValues[2] } }
                            .filter { !it.isNullOrBlank() }
                            .map { it as String }
                            .plus(getContents(doc, "/Project/PropertyGroup/Configuration"))
                            .distinct()
                            .map { Configuration(it) }
                            .toList()

                    val frameworks = getContents(doc, "/Project/PropertyGroup/TargetFrameworks")
                            .flatMap { it.split(';').asSequence() }
                            .plus(getContents(doc, "/Project/PropertyGroup/TargetFramework"))
                            .distinct()
                            .map { Framework(it) }
                            .toList()

                    val runtimes = getContents(doc, "/Project/PropertyGroup/RuntimeIdentifiers")
                            .flatMap { it.split(';').asSequence() }
                            .plus(getContents(doc, "/Project/PropertyGroup/RuntimeIdentifier"))
                            .distinct()
                            .map { Runtime(it) }
                            .toList()

                    val references = getAttributes(doc, "/Project/ItemGroup/PackageReference[@Include]", "Include")
                            .filter { !it.isBlank() }
                            .plus(
                                    getAttributes(doc, "/Project/ItemGroup/Reference[@Include]", "Include")
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

                    Solution(listOf(Project(path, configurations, frameworks, runtimes, references, targets, generatePackageOnBuild)))
                }
            } ?: Solution(emptyList())

    private fun getElements(doc: Document, xpath: String): Sequence<Element> = buildSequence {
        val nodes = xPath.evaluate(xpath, doc, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodes.length) {
            val element = nodes.item(i) as Element
            yield(element)
        }
    }

    private fun getContents(doc: Document, xpath: String): Sequence<String> =
            getElements(doc, xpath).map { it.textContent }.filter { !it.isNullOrBlank() }

    private fun getAttributes(doc: Document, xpath: String, attributeName: String): Sequence<String> =
            getElements(doc, xpath).map { it.getAttribute(attributeName) }.filter { !it.isNullOrBlank() }

    private val xPath = XPathFactory.newInstance().newXPath()

    companion object {
        private val ConditionPattern: Regex = Regex("'\\$\\(Configuration\\)([^']*)' == '([^|]*)([^']*)'", RegexOption.IGNORE_CASE)
        private val PathPattern: Pattern = Pattern.compile("^.+\\.(proj|csproj|vbproj)$", CASE_INSENSITIVE)
    }
}