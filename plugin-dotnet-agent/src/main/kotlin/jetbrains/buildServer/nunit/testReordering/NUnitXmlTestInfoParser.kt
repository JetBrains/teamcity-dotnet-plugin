package jetbrains.buildServer.nunit.testReordering

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.find
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import javax.xml.xpath.XPathExpressionException

class NUnitXmlTestInfoParser(private val _xmlDocumentService: XmlDocumentService) {
    fun parse(text: String): List<TestInfo> {
        if (text.isBlank()) {
            return emptyList()
        }

        try {
            val stringBytes = removeUTF8BOM(text).toByteArray(StandardCharsets.UTF_8)
            val inputStream = ByteArrayInputStream(stringBytes)
            return parseXmlDocument(_xmlDocumentService.deserialize(inputStream)).toList()
        } catch (e: XPathExpressionException) {
            LOG.error(e)
            throw RunBuildException(ERROR_DURING_PARSING_ERROR_MESSAGE)
        }
    }

    private fun parseXmlDocument(doc: Document) = sequence {
        for (assemblyElement in doc.find<Node>(ASSEMBLY_XPATH)) {
            val assembly = assemblyElement.attributes.getNamedItem(FULLNAME_ATTR)
                ?.nodeValue
                ?.takeIf { it.isNotBlank() }
                ?.let { File(it) }

            for (testFixture in assemblyElement.find<Node>(TEST_FIXTURE_XPATH)) {
                val testFixtureName = testFixture.attributes.getNamedItem(FULLNAME_ATTR)?.nodeValue
                if (testFixtureName.isNullOrBlank()) {
                    continue
                }

                for (testElement in testFixture.find<Node>(TEST_CASE_XPATH)) {
                    val fullMethodName = testElement.attributes.getNamedItem(FULLNAME_ATTR)?.nodeValue

                    yield(TestInfo(assembly, testFixtureName, fullMethodName))
                }
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(NUnitXmlTestInfoParser::class.java.name)
        private const val ERROR_DURING_PARSING_ERROR_MESSAGE = "Error during parsing NUnit test list xml document"
        private const val FULLNAME_ATTR = "fullname"
        private const val ASSEMBLY_XPATH = "//test-suite[@type='Assembly']"
        private const val TEST_FIXTURE_XPATH = ".//test-suite[@type='TestFixture']"
        private const val TEST_CASE_XPATH = ".//test-case"

        // FEFF because this is the Unicode char represented by the UTF-8 byte order mark (EF BB BF).
        private const val UTF8_BOM = "\uFEFF"
        private fun removeUTF8BOM(s: String): String {
            if (s.startsWith(UTF8_BOM)) {
                return s.substring(1)
            }
            return s
        }
    }
}