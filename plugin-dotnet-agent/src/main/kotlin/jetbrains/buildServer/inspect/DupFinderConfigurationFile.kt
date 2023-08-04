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

import jetbrains.buildServer.DocElement
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_COST
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_FIELDS_NAME
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_LITERALS
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_LOCAL_VARIABLES_NAME
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_TYPES
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_EXCLUDE_BY_OPENING_COMMENT
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_EXCLUDE_FILES
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_INCLUDE_FILES
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_NORMALIZE_TYPES
import jetbrains.buildServer.util.OSType
import java.io.OutputStream

class DupFinderConfigurationFile(
        private val _parametersService: ParametersService,
        private val _xmlWriter: XmlWriter,
        private val _pathsService: PathsService,
        private val _pathMatcher: PathMatcher,
        private val _virtualContext: VirtualContext)
    : ConfigurationFile {

    override fun create(destinationStream: OutputStream, outputFile: Path, cachesHomeDirectory: Path?, debug: Boolean) =
        _xmlWriter.write(
                DocElement("DupFinderOptions",
                        DocElement("ShowStats", true.toString()),
                        DocElement("ShowText", true.toString()),
                        DocElement("Debug", if(debug) debug.toString() else null),
                        DocElement("DiscardFieldsName", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_FIELDS_NAME)?.toBoolean()?.toString()),
                        DocElement("DiscardLiterals", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_LITERALS)?.toBoolean()?.toString()),
                        DocElement("DiscardLocalVariablesName", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_LOCAL_VARIABLES_NAME)?.toBoolean()?.toString()),
                        DocElement("DiscardTypes", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_TYPES)?.toBoolean()?.toString()),
                        DocElement("NormalizeTypes", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_NORMALIZE_TYPES)?.toBoolean()?.toString()),
                        DocElement("DiscardCost", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_COST)),
                        DocElement("OutputFile", if(!outputFile.path.isNullOrEmpty()) outputFile.path else null),
                        DocElement("CachesHomeDirectory", if(!cachesHomeDirectory?.path.isNullOrEmpty()) cachesHomeDirectory?.path else null),
                        createDocElement("ExcludeFilesByStartingCommentSubstring", "Substring", SETTINGS_EXCLUDE_BY_OPENING_COMMENT),
                        createDocElement("ExcludeCodeRegionsByNameSubstring", "Substring", SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS),
                        createDocElement("ExcludeFiles", "Pattern", SETTINGS_EXCLUDE_FILES) { parseFileMask(it) },
                        createDocElement("InputFiles", "Pattern", SETTINGS_INCLUDE_FILES) { parseFileMask(it) }
                ),
                destinationStream
        )

    private fun parseFileMask(masks: List<String>): List<String> {
        if (!masks.any() || _virtualContext.targetOSType == OSType.WINDOWS) {
            return masks
        }

        val workingDirectory = _pathsService.getPath(PathType.WorkingDirectory)
        return _pathMatcher
                .match(workingDirectory, masks)
                .map { _virtualContext.resolvePath(it.absolutePath) }
    }

    private fun createDocElement(groupElementName: String, elementName: String, paramName: String, mapper: (List<String>) -> List<String> = { strs -> strs }): DocElement {
        var subElements = mapper(
                _parametersService.tryGetParameter(ParameterType.Runner, paramName)
                        ?.lines()
                        ?.filter { it.isNotBlank() }
                        ?: emptyList()
                )
                .map { DocElement(elementName, it) }
                .toList()

        if(subElements.any() == true) {
            return DocElement(groupElementName, subElements.asSequence())
        }
        else {
            return DocElement(groupElementName, null as String?)
        }
    }
}