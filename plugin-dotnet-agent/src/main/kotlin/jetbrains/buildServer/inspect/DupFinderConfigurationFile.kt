package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.build
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
import org.w3c.dom.Document
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
                E("DupFinderOptions",
                        E("ShowStats", true.toString()),
                        E("ShowText", true.toString()),
                        E("Debug", if(debug) debug.toString() else null),
                        E("DiscardFieldsName", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_FIELDS_NAME)?.toBoolean()?.toString()),
                        E("DiscardLiterals", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_LITERALS)?.toBoolean()?.toString()),
                        E("DiscardLocalVariablesName", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_LOCAL_VARIABLES_NAME)?.toBoolean()?.toString()),
                        E("DiscardTypes", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_TYPES)?.toBoolean()?.toString()),
                        E("NormalizeTypes", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_NORMALIZE_TYPES)?.toBoolean()?.toString()),
                        E("DiscardCost", _parametersService.tryGetParameter(ParameterType.Runner, SETTINGS_DISCARD_COST)),
                        E("OutputFile", if(!outputFile.path.isNullOrEmpty()) outputFile.path else null),
                        E("CachesHomeDirectory", if(!cachesHomeDirectory?.path.isNullOrEmpty()) cachesHomeDirectory?.path else null),
                        createElement("ExcludeFilesByStartingCommentSubstring", "Substring", SETTINGS_EXCLUDE_BY_OPENING_COMMENT),
                        createElement("ExcludeCodeRegionsByNameSubstring", "Substring", SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS),
                        createElement("ExcludeFiles", "Pattern", SETTINGS_EXCLUDE_FILES) { parseFileMask(it) },
                        createElement("InputFiles", "Pattern", SETTINGS_INCLUDE_FILES) { parseFileMask(it) }
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

    private fun createElement(groupElementName: String, elementName: String, paramName: String, mapper: (List<String>) -> List<String> = { strs -> strs }): E {
        var subElements = mapper(
                _parametersService.tryGetParameter(ParameterType.Runner, paramName)
                        ?.lines()
                        ?.filter { it.isNotBlank() }
                        ?: emptyList()
                )
                .map { E(elementName, it) }
                .toList()

        if(subElements.any() == true) {
            return E(groupElementName, subElements.asSequence())
        }
        else {
            return E(groupElementName, null as String?)
        }
    }
}