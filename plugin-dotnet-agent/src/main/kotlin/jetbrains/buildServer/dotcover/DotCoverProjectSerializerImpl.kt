package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.XmlDocumentService
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.OutputStream

class DotCoverProjectSerializerImpl(
        private val _xmlDocumentService: XmlDocumentService,
        private val _argumentsService: ArgumentsService,
        private val _coverageFilterProvider: CoverageFilterProvider)
    : DotCoverProjectSerializer{

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun serialize(project: DotCoverProject, outputStream: OutputStream) {
        val doc = _xmlDocumentService.create()
        val coverageParamsElement = doc.createElement(COVERAGE_PARAMS_ELEMENT)
        coverageParamsElement.setAttributeNS(HTTP_WWW_W3_NS_URI, XMLNS_XSD_NS, XMLNS_XSD_NS_URI)
        coverageParamsElement.setAttributeNS(HTTP_WWW_W3_NS_URI, XMLNS_XSI_NS, XMLNS_XSI_NS_URI)
        doc.appendChild(coverageParamsElement)

        coverageParamsElement.appendChild(createSimpleElement(doc, EXECUTABLE_ELEMENT, project.commandLineToGetCoverage.executableFile.absolutePath))
        coverageParamsElement.appendChild(createSimpleElement(doc, ARGUMENTS_ELEMENT, _argumentsService.combine(project.commandLineToGetCoverage.arguments.map { it.value }.asSequence())))
        val workingDirectory = project.commandLineToGetCoverage.workingDirectory
        coverageParamsElement.appendChild(createSimpleElement(doc, WORKING_DIR_ELEMENT, workingDirectory.absolutePath))
        coverageParamsElement.appendChild(createSimpleElement(doc, OUTPUT_ELEMENT, project.snapshotFile.absolutePath))

        val filtersElement = doc.createElement(FILTERS_ELEMENT)
        val includeFiltersElement = doc.createElement(INCLUDE_FILTERS_ELEMENT)
        val excludeFiltersElement = doc.createElement(EXCLUDE_FILTERS_ELEMENT)

        for (filter in _coverageFilterProvider.filters) {
            val filterElement = createFilter(doc, filter)
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (filter.type) {
                CoverageFilter.CoverageFilterType.Include -> includeFiltersElement.appendChild(filterElement)
                CoverageFilter.CoverageFilterType.Exclude -> excludeFiltersElement.appendChild(filterElement)
            }
        }

        if (includeFiltersElement.hasChildNodes()) {
            filtersElement.appendChild(includeFiltersElement)
        }

        if (excludeFiltersElement.hasChildNodes()) {
            filtersElement.appendChild(excludeFiltersElement)
        }

        if (filtersElement.hasChildNodes()) {
            coverageParamsElement.appendChild(filtersElement)
        }

        val attributeFiltersElement = doc.createElement(ATTRIBUTE_FILTERS_ELEMENT)
        for (filter in _coverageFilterProvider.attributeFilters) {
            val filterElement = createAttributeFilter(doc, filter)
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (filter.type) {
                CoverageFilter.CoverageFilterType.Exclude -> attributeFiltersElement.appendChild(filterElement)
            }
        }

        if (attributeFiltersElement.hasChildNodes()) {
            coverageParamsElement.appendChild(attributeFiltersElement)
        }

        _xmlDocumentService.serialize(doc, outputStream)
    }

    private fun createSimpleElement(doc: Document, name: String, value: String): Element {
        val executableElement = doc.createElement(name)
        executableElement.textContent = value
        return executableElement
    }

    private fun createFilter(doc: Document, coverageFilter: CoverageFilter): Element {
        val filter = doc.createElement(FILTER_ENTRY_ELEMENT)
        filter.appendChild(createSimpleElement(doc, MODULE_MASK_ELEMENT, coverageFilter.moduleMask))
        filter.appendChild(createSimpleElement(doc, CLASS_MASK_ELEMENT, coverageFilter.classMask))
        filter.appendChild(createSimpleElement(doc, FUNCTION_MASK_ELEMENT, coverageFilter.functionMask))
        return filter
    }

    private fun createAttributeFilter(doc: Document, coverageFilter: CoverageFilter): Element {
        val filter = doc.createElement(ATTRIBUTE_FILTER_ENTRY_ELEMENT)
        filter.appendChild(createSimpleElement(doc, CLASS_MASK_ELEMENT, coverageFilter.classMask))
        return filter
    }

    companion object {
        private val COVERAGE_PARAMS_ELEMENT = "CoverageParams"
        private val HTTP_WWW_W3_NS_URI = "http://www.w3.org/2000/xmlns/"
        private val XMLNS_XSD_NS = "xmlns:xsd"
        private val XMLNS_XSD_NS_URI = "http://www.w3.org/2001/XMLSchema"
        private val XMLNS_XSI_NS = "xmlns:xsi"
        private val XMLNS_XSI_NS_URI = "http://www.w3.org/2001/XMLSchema-instance"
        private val EXECUTABLE_ELEMENT = "Executable"
        private val ARGUMENTS_ELEMENT = "Arguments"
        private val WORKING_DIR_ELEMENT = "WorkingDir"
        private val OUTPUT_ELEMENT = "Output"
        private val FILTERS_ELEMENT = "Filters"
        private val INCLUDE_FILTERS_ELEMENT = "IncludeFilters"
        private val EXCLUDE_FILTERS_ELEMENT = "ExcludeFilters"
        private val FILTER_ENTRY_ELEMENT = "FilterEntry"
        private val ATTRIBUTE_FILTER_ENTRY_ELEMENT = "AttributeFilterEntry"
        private val MODULE_MASK_ELEMENT = "ModuleMask"
        private val FUNCTION_MASK_ELEMENT = "FunctionMask"
        private val CLASS_MASK_ELEMENT = "ClassMask"
        private val ATTRIBUTE_FILTERS_ELEMENT = "AttributeFilters"
    }
}