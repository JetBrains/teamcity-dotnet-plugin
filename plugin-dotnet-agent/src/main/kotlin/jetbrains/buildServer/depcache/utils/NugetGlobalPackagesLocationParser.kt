package jetbrains.buildServer.depcache.utils

object NugetGlobalPackagesLocationParser {

    private val GLOBAL_PACKAGES_REGEX = Regex("""global-packages:\s*(.*)""")

    fun fromCommandLineOutput(commandLineOutput: String): String? {
        val matchResult = GLOBAL_PACKAGES_REGEX.find(commandLineOutput)
        return matchResult?.groupValues?.get(1)
    }
}