package jetbrains.buildServer.script

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.*
import jetbrains.buildServer.tools.*
import jetbrains.buildServer.web.openapi.PluginDescriptor
import java.io.File

class CSharpScriptToolProvider(
        private val _toolService: ToolService,
        private val _toolType: ToolType)
    : ServerToolProviderAdapter() {

    override fun getType() = _toolType

    override fun getAvailableToolVersions(): MutableCollection<out ToolVersion> =
            _toolService.getTools(type, ScriptConstants.CLT_TOOL_TYPE_ID).toMutableList()

    override fun tryGetPackageVersion(toolPackage: File) =
            _toolService.tryGetPackageVersion(type, toolPackage, ScriptConstants.CLT_TOOL_TYPE_ID) ?: super.tryGetPackageVersion(toolPackage)

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File) =
            _toolService.fetchToolPackage(type, toolVersion, targetDirectory, ScriptConstants.CLT_TOOL_TYPE_ID)

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) =
            _toolService.unpackToolPackage(toolPackage, "", targetDirectory, ScriptConstants.CLT_TOOL_TYPE_ID)

    companion object {
        private val LOG: Logger = Logger.getInstance(CSharpScriptToolProvider::class.java.name)
    }
}