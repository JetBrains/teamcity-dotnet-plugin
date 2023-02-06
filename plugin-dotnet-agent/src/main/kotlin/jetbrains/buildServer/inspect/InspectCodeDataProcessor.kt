/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                when (e.name.lowercase()) {
                    "issuetype" -> {
                        val severity = when (e["Severity"]?.lowercase()) {
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