package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.JsonParser
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.agent.Logger
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class JsonVisualStudioInstanceParser(private val _jsonParser: JsonParser) : VisualStudioInstanceParser {
    override fun tryParse(stream: InputStream): ToolInstance? {
        BufferedReader(InputStreamReader(stream)).use {
            val state = _jsonParser.tryParse<VisualStudioState>(it, VisualStudioState::class.java)
            val installationPath = state?.installationPath;
            val displayVersion = state?.catalogInfo?.productDisplayVersion ?: state?.installationVersion
            val productLineVersion = state?.catalogInfo?.productLineVersion
            if (installationPath.isNullOrBlank() || displayVersion.isNullOrBlank() || productLineVersion.isNullOrBlank()) {
                LOG.debug("Invalid Visual Studio state.")
                return null
            }

            val productId = state.product?.id;
            if (TeamExplorerProductId.equals(productId, true)) {
                LOG.debug("Ignore Team Explorer installation.")
                return null
            }

            return ToolInstance(
                    ToolInstanceType.VisualStudio,
                    File(installationPath, DefaultDevenvPath),
                    Version.parse(displayVersion.replace(' ', '-')),
                    Version.parse(productLineVersion),
                    Platform.Default)
        }
    }

    open class VisualStudioState {
        var installationPath: String? = null
        var installationVersion: String? = null
        var catalogInfo: CatalogInfo? = null
        var product: ProductInfo? = null
    }

    class CatalogInfo {
        var productDisplayVersion: String? = null
        var productLineVersion: String? = null
    }

    class ProductInfo {
        var id: String? = null
    }

    companion object {
        private val LOG = Logger.getLogger(JsonVisualStudioInstanceParser::class.java)
        internal const val TeamExplorerProductId = "Microsoft.VisualStudio.Product.TeamExplorer"
        private val DefaultDevenvPath = File(File("Common7"), "IDE").path
    }
}