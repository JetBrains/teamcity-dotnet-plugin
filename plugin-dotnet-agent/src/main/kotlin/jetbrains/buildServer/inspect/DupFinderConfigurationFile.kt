

package jetbrains.buildServer.inspect

import jetbrains.buildServer.XmlElement
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
    private val _virtualContext: VirtualContext
) : ConfigurationFile {

    override fun create(destinationStream: OutputStream, outputFile: Path, cachesHomeDirectory: Path?, debug: Boolean) =
        _xmlWriter.write(
            XmlElement("DupFinderOptions",
                XmlElement("ShowStats", true.toString()),
                XmlElement("ShowText", true.toString()),
                XmlElement("Debug", if (debug) debug.toString() else null),
                XmlElement("DiscardFieldsName", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_FIELDS_NAME)?.toBoolean()?.toString()),
                XmlElement("DiscardLiterals", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_LITERALS)?.toBoolean()?.toString()),
                XmlElement("DiscardLocalVariablesName", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_LOCAL_VARIABLES_NAME)?.toBoolean()?.toString()),
                XmlElement("DiscardTypes", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_TYPES)?.toBoolean()?.toString()),
                XmlElement("NormalizeTypes", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_NORMALIZE_TYPES)?.toBoolean()?.toString()),
                XmlElement("DiscardCost", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_COST)),
                XmlElement("OutputFile", if (!outputFile.path.isNullOrEmpty()) outputFile.path else null),
                XmlElement("CachesHomeDirectory", if (!cachesHomeDirectory?.path.isNullOrEmpty()) cachesHomeDirectory?.path else null),
                createXmlElement("ExcludeFilesByStartingCommentSubstring", "Substring", SETTINGS_EXCLUDE_BY_OPENING_COMMENT),
                createXmlElement("ExcludeCodeRegionsByNameSubstring", "Substring", SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS),
                createXmlElement("ExcludeFiles", "Pattern", SETTINGS_EXCLUDE_FILES) { parseFileMask(it) },
                createXmlElement("InputFiles", "Pattern", SETTINGS_INCLUDE_FILES) { parseFileMask(it) }
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

    private fun createXmlElement(groupElementName: String, elementName: String, paramName: String, mapper: (List<String>) -> List<String> = { strs -> strs }): XmlElement {
        var subElements = mapper(
            _parametersService.tryGetParameter(ParameterType.Runner, paramName)
                ?.lines()
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        )
            .map { XmlElement(elementName, it) }
            .toList()

        if (subElements.any() == true) {
            return XmlElement(groupElementName, subElements.asSequence())
        } else {
            return XmlElement(groupElementName, null as String?)
        }
    }
}