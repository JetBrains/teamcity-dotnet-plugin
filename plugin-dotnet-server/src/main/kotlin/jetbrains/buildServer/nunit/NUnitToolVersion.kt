package jetbrains.buildServer.nunit

import jetbrains.buildServer.nunit.NUnitRunnerConstants.MIN_NUPKG_VERSION
import jetbrains.buildServer.tools.available.DownloadableToolVersion
import jetbrains.buildServer.tools.utils.SemanticVersion
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
        private const val EMPTY_VERSION = ""

        // e.g.: NUnit.ConsoleRunner.4.0.0.nupkg
        // we use a .nupkg package for NUnit tools since 4.0.0, as NUnit stopped distributing ZIP archives
        private val nupkgPackagePattern: Pattern = Pattern.compile(
            "NUnit\\.ConsoleRunner\\." + "([\\d\\.]+)" + "\\.nupkg",
            Pattern.CASE_INSENSITIVE
        )

        // e.g.: NUnit.Console-3.19.2.zip
        // we use a .zip package for NUnit tools up to version 4.0.0
        private val zipPackagePattern: Pattern = Pattern.compile(
            "NUnit\\.Console" + "-([\\d\\.]+)" + "\\.zip",
            Pattern.CASE_INSENSITIVE
        )

        // provide a .nupkg package if the version is 4.0.0 and higher
        // provide a .zip if the version is below 4.0.0
        fun getPackageVersion(toolPackageName: String): String {
            return extractVersion(toolPackageName, nupkgPackagePattern) { version ->
                SemanticVersion.compareAsVersions(version, MIN_NUPKG_VERSION) >= 0
            } ?: extractVersion(toolPackageName, zipPackagePattern) { version ->
                SemanticVersion.compareAsVersions(version, MIN_NUPKG_VERSION) < 0
            } ?: EMPTY_VERSION
        }

        private fun extractVersion(
            packageName: String,
            pattern: Pattern,
            isValid: (String) -> Boolean
        ): String? {
            val matcher = pattern.matcher(packageName)

            return if (matcher.find()) {
                val version = matcher.group(1)
                if (isValid(version)) version else null
            } else {
                null
            }
        }
    }
}
