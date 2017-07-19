package jetbrains.buildServer.dotnet

import java.util.regex.Pattern

class DotnetSdkVersionProviderImpl : DotnetSdkVersionProvider {
    private val VERSION_PATTERN = Pattern.compile("^\\s+Version:\\s+(\\d+\\.\\d+\\.\\d+[^\\s]*)", Pattern.MULTILINE)

    override fun getSdkVersion(output: String): String {
        val matcher = VERSION_PATTERN.matcher(output)
        val version = if (matcher.find()) matcher.group(1) else output
        return version.trim()
    }
}