package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.JsonParser
import jetbrains.buildServer.agent.Version
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class JsonVisualStudioInstanceParser(private val _jsonParser: JsonParser) : VisualStudioInstanceParser {
    override fun tryParse(stream: InputStream): VisualStudioInstance? {
        BufferedReader(InputStreamReader(stream)).use {
            val state = _jsonParser.tryParse<VisualStudioState>(it, VisualStudioState::class.java)
            val installationPath = state?.installationPath;
            val fileName = state?.launchParams?.fileName ?: DefaultDevenvPath.path
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

            return VisualStudioInstance(
                    File(File(installationPath), fileName).parentFile,
                    Version.parse(displayVersion),
                    Version.parse(productLineVersion))
        }
    }

    open class VisualStudioState {
        var installationPath: String? = null
        var installationVersion: String? = null
        var launchParams: LaunchParams? = null
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

    class LaunchParams {
        var fileName: String? = null
    }

    companion object {
        private val LOG = Logger.getLogger(JsonVisualStudioInstanceParser::class.java)
        internal const val TeamExplorerProductId = "Microsoft.VisualStudio.Product.TeamExplorer"
        private val DefaultDevenvPath = File(File(File("Common7"), "IDE"), "devenv.exe")
    }
}