package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.JsonParser
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.dotnet.Platform
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class JsonVisualStudioInstanceParser(private val _jsonParser: JsonParser) : VisualStudioInstanceParser {
    override fun tryParse(stream: InputStream): ToolInstance? {
        BufferedReader(InputStreamReader(stream)).use {
            val state = _jsonParser.tryParse(it, VisualStudioState::class.java)
            val installationPath = state?.installationPath;
            val detailedVersion = getDetailedVersion(state)
            val productLineVersion = state?.catalogInfo?.productLineVersion
            if (installationPath.isNullOrBlank() || detailedVersion == null || productLineVersion.isNullOrBlank()) {
                LOG.info("Invalid Visual Studio state: $state")
                return null
            }
            if (LOG.isDebugEnabled) {
                LOG.debug("VisualStudio state: $state")
            }

            val productId = state.product?.id;
            if (TeamExplorerProductId.equals(productId, true)) {
                LOG.debug("Ignore Team Explorer installation.")
                return null
            }
            var baseVersion = Version.parse(productLineVersion)
            // starting from Visual Studio 2026 there is no base version in "YYYY" format in the "state.json"
            // we replace "18" with "2026" to keep the "VS2026" agent parameter name consistent with "VS2022" and others
            // maybe there are other reasons for this replacement, not sure...
            if (baseVersion.major == 18) {
                baseVersion = Version.parse("2026")
            }
            return ToolInstance(
                    ToolInstanceType.VisualStudio,
                    File(installationPath, DefaultDevenvPath),
                    detailedVersion,
                    baseVersion,
                    Platform.Default)
        }
    }

    private fun getDetailedVersion(state: VisualStudioState?): Version? {
        val semanticVersion = state?.catalogInfo?.productSemanticVersion
        if (semanticVersion != null) {
            val parsed = Version.parse(semanticVersion)
            if (!parsed.isEmpty()) {
                return semanticVersionWithoutMetadata(parsed)
            }
        }
        val fallbackVersion = state?.catalogInfo?.productDisplayVersion ?: state?.installationVersion
        if (fallbackVersion.isNullOrBlank()) {
            return null
        }
        return Version.parse(fallbackVersion.replace(' ', '-'))
    }

    private fun semanticVersionWithoutMetadata(version: Version): Version {
        return if (version.release != null) {
            Version(major = version.major, minor = version.minor, patch = version.patch, release = version.release)
        } else {
            Version(
                major = version.major,
                minor = version.minor,
                patch = version.patch,
                build = version.build,
                minorBuild = version.minorBuild,
            )
        }
    }

    open class VisualStudioState {
        var installationPath: String? = null
        var installationVersion: String? = null
        var catalogInfo: CatalogInfo? = null
        var product: ProductInfo? = null

        override fun toString(): String = """
        [
            productId=${product?.id},
            installationPath=${installationPath},
            installationVersion=${installationVersion},
            productSemanticVersion=${catalogInfo?.productSemanticVersion},
            productDisplayVersion=${catalogInfo?.productDisplayVersion},
            productLineVersion=${catalogInfo?.productLineVersion},
        ]""".trimIndent()
    }

    class CatalogInfo {
        var productSemanticVersion: String? = null
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