package jetbrains.buildServer.nunit.nUnitProject

import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.nunit.NUnitSettings
import java.io.OutputStream

class NUnitProjectSerializer(
    private val _nUnitSettings: NUnitSettings,
    private val _documentService: XmlDocumentService,
    private val _pathsService: PathsService
) {
    fun create(project: NUnitProject, outputStream: OutputStream) {
        val doc = _documentService.create()

        // Config element
        val configElement = doc.createElement(CONFIG_ELEMENT).also {
            it.setAttribute(NAME_ATTR, DEFAULT_CONFIG_NAME)
        }

        for (assembly in project.testingAssemblies) {
            val assemblyElement = doc.createElement(ASSEMBLY_ELEMENT)
            assemblyElement.setAttribute(PATH_ATTR, assembly.path)
            configElement.appendChild(assemblyElement)
        }

        val appConfigFileStr = _nUnitSettings.appConfigFile
        if (!appConfigFileStr.isNullOrBlank()) {
            val appConfigFile = _pathsService.resolvePath(PathType.Checkout, appConfigFileStr)
            configElement.setAttribute(CONFIG_FILE_ATTR, appConfigFile.toAbsolutePath().toString())
        }

        // Settings element
        val settingsElement = doc.createElement(SETTINGS_ELEMENT).also {
            it.setAttribute(ACTIVE_CONFIG_ATTR, DEFAULT_CONFIG_NAME)
            it.setAttribute(APPBASE_ATTR, project.appBase.path)
        }

        val nUnitProjectElement = doc.createElement(NUNIT_PROJECT_ELEMENT).also {
            it.appendChild(settingsElement)
            it.appendChild(configElement)
        }

        doc.appendChild(nUnitProjectElement)
        _documentService.serialize(doc, outputStream)
    }

    companion object {
        private const val CONFIG_ELEMENT = "Config"
        private const val ASSEMBLY_ELEMENT = "assembly"
        private const val SETTINGS_ELEMENT = "Settings"
        private const val NUNIT_PROJECT_ELEMENT = "NUnitProject"
        private const val NAME_ATTR = "name"
        private const val PATH_ATTR = "path"
        private const val ACTIVE_CONFIG_ATTR = "activeconfig"
        private const val DEFAULT_CONFIG_NAME = "active"
        private const val APPBASE_ATTR = "appbase"
        private const val CONFIG_FILE_ATTR = "configfile"
    }
}
