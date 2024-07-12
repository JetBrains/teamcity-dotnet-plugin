package jetbrains.buildServer.dotnet.coverage.dotcover

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageReportGeneratorRunner
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotcover.utils.XmlXppAbstractParser
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.VersionComparatorUtil
import java.io.File
import java.io.IOException

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class DotCoverVersionFetcher {

    @Throws(IOException::class)
    fun getDotCoverVersionString(params: DotnetCoverageParameters,
                                 runner: DotnetCoverageReportGeneratorRunner): String? {
        val version: File = FileUtil.createTempFile(params.getTempDirectory(), "dotCover", "Version", true)
        val code: Int = runner.runReportGenerator("Get dotCover version", listOf("version", version.path))
        try {
            if ((code != 0) || !version.isFile || (version.length() < 10)) return ""
            val versionTextBuilder: StringBuilder = StringBuilder()
            object : XmlXppAbstractParser() {
                override fun getRootHandlers(): List<XmlHandler> {
                    return listOf(elementsPath(object : Handler {
                        override fun processElement(reader: XmlElementInfo): XmlReturn {
                            return reader.visitText(object : TextHandler {
                                override fun setText(text: String) {
                                    versionTextBuilder.append(text)
                                }
                            })
                        }
                    }, "VersionInfo", "Version"))
                }
            }.parse(version)
            return versionTextBuilder.toString()
        } catch (e: IOException) {
            LOG.debug("Failed to parse DotCover version file. " + e.message, e)
        } finally {
            FileUtil.delete(version)
        }
        return null
    }

    @Throws(IOException::class)
    fun getDotCoverVersion(dotCoverVersionString: String?,
                           params: DotnetCoverageParameters): DotCoverVersion {
        if (dotCoverVersionString == null) {
            return defaultVersion
        }
        if (("" == dotCoverVersionString)) return DotCoverVersion.DotCover_1_0
        if (dotCoverVersionString.startsWith("1.1.")) return DotCoverVersion.DotCover_1_1
        if (dotCoverVersionString.startsWith("1.2.")) return DotCoverVersion.DotCover_1_2
        if (dotCoverVersionString.startsWith("2.0.")) return DotCoverVersion.DotCover_2_0
        if (dotCoverVersionString.startsWith("2.1.")) return DotCoverVersion.DotCover_2_1
        if (dotCoverVersionString.startsWith("2.2.")) return DotCoverVersion.DotCover_2_2
        if (dotCoverVersionString.startsWith("2.5.")) return DotCoverVersion.DotCover_2_5
        if (dotCoverVersionString.startsWith("2.6.")) return DotCoverVersion.DotCover_2_6
        if (dotCoverVersionString.startsWith("2.7.")) return DotCoverVersion.DotCover_2_7
        if (dotCoverVersionString.startsWith("3.0.")) return DotCoverVersion.DotCover_3_0
        if (dotCoverVersionString.startsWith("3.1")) return DotCoverVersion.DotCover_3_1
        if (dotCoverVersionString.startsWith("3.2")) return DotCoverVersion.DotCover_3_2
        if (dotCoverVersionString.startsWith("10.0")) return DotCoverVersion.DotCover_10_0
        if (VersionComparatorUtil.compare(dotCoverVersionString, "2016") >= 0) return DotCoverVersion.DotCover_2016AndHigher
        return handleUnknownVersion(params, dotCoverVersionString)
    }

    private val defaultVersion: DotCoverVersion
        get() = DotCoverVersion.DotCover_2016AndHigher

    private fun handleUnknownVersion(params: DotnetCoverageParameters,
                                     versionText: String): DotCoverVersion {
        params.getBuildLogger()
            .warning("DotCover version is $versionText. This version is newer than TeamCity and may not be fully supported.")
        return defaultVersion
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(DotCoverVersionFetcher::class.java.name)
    }
}