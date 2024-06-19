package jetbrains.buildServer.nunit

import jetbrains.buildServer.tools.available.DownloadableToolVersion
import java.util.regex.Pattern

class NUnitToolVersion(private val _name: String, private val _downloadUrl: String) : DownloadableToolVersion {
    private val _version: String = getPackageVersion(_name)

    val isValid
        get() = _version.isNotEmpty()

    override fun getType() = NUnitToolProvider.CLT_TOOL_TYPE
    override fun getVersion() = _version
    override fun getId() = NUnitRunnerConstants.NUNIT_PACKAGE_ID + "." + version
    override fun getDisplayName() = NUnitRunnerConstants.NUNIT_TOOL_TYPE_NAME + " " + version
    override fun getDownloadUrl() = _downloadUrl
    override fun getDestinationFileName() = _name

    companion object {
        private const val NUNIT_PACKAGE_EXT: String = ".zip"
        private val NamePattern: Pattern = Pattern.compile(
            NUnitRunnerConstants.NUNIT_TOOL_TYPE_ID + "-([\\d\\.]+)" + NUNIT_PACKAGE_EXT,
            Pattern.CASE_INSENSITIVE
        )

        fun getPackageVersion(toolPackageName: String): String {
            val matcher = NamePattern.matcher(toolPackageName)
            if (!matcher.find()) {
                return ""
            }

            return matcher.group(1)
        }
    }
}
