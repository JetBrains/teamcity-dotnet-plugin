package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.DataProcessor
import jetbrains.buildServer.agent.DataProcessorContext
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.inspections.*

class InspectCodeDataProcessor(
        private val _fileSystem: FileSystemService,
        private val _xmlReader: XmlReader,
        private val _reporter: InspectionReporter)
    : DataProcessor {
    override fun getType() = InspectCodeConstants.DATA_PROCESSOR_TYPE

    override fun processData(context: DataProcessorContext) {
        _reporter.markBuildAsInspectionsBuild()
        _fileSystem.read(context.file) {
            val inspectionTypeSeverities = mutableMapOf<String, InspectionSeverityValues>()
            for (e in _xmlReader.read(it)) {
                when (e.name.toLowerCase()) {
                    "issuetype" -> {
                        val severity = when (e["Severity"]?.toLowerCase()) {
                            "error" -> InspectionSeverityValues.ERROR
                            "warning" -> InspectionSeverityValues.WARNING
                            else -> InspectionSeverityValues.INFO
                        }

                        val type = InspectionTypeInfo()
                        type.id = e["Id"]
                        type.name = e["Description"]
                        type.category = e["Category"]
                        type.description = e["WikiUrl"] ?: "" //optional
                        if (type.id.isNotBlank()) {
                            inspectionTypeSeverities[type.id] = severity
                            _reporter.reportInspectionType(type)
                        }
                    }

                    "issue" -> {
                        val info = InspectionInstance()
                        info.inspectionId = e["TypeId"]
                        info.filePath = e["File"]?.replace("\\", "/")
                        info.line = e["Line"]?.toIntOrNull() ?: 0
                        info.message = e["Message"]
                        if (info.inspectionId.isNotBlank()) {
                            inspectionTypeSeverities[info.inspectionId]?.let { severity ->
                                info.addAttribute(InspectionAttributesId.SEVERITY.toString(), listOf(severity.toString()))
                                _reporter.reportInspection(info)
                            }
                        }
                    }
                }
            }
        }
    }
}