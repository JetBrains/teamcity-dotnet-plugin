package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.agent.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.File

class SdkRegistryProvider(
        private val _windowsRegistry: WindowsRegistry,
        private val _sdkInstanceFactory: ToolInstanceFactory)
    : ToolInstanceProvider {
    @Cacheable("ListOfSdkFromRegistry", sync = true)
    override fun getInstances(): Collection<ToolInstance> {
        val sdks = mutableMapOf<String, Sdk>()
        for (regKey in RegKeys) {
            _windowsRegistry.accept(
                    regKey,
                    object : WindowsRegistryVisitor {
                        override fun visit(key: WindowsRegistryKey) = true
                        override fun visit(value: WindowsRegistryValue): Boolean {
                            val parts = value.key.parts.takeLast(3)
                            val majorKey = parts[0]
                            val minorKey = parts[1]
                            val name = parts[2].toLowerCase()
                            var key: String? = null
                            var platform: Platform = Platform.Default
                            var toolInstanceType: ToolInstanceType = ToolInstanceType.WindowsSDK
                            var baseVersion: Version = Version.Empty
                            if (value.type == WindowsRegistryValueType.Str && value.text.isNotBlank()) {
                                // Windows SDK
                                if ("Windows".equals(majorKey, true)) {
                                    WinSdkVersionRegex.matchEntire(minorKey)?.groupValues?.let { values ->
                                        var versionStr = values[1]
                                        val release = values[2]
                                        if (release.isNotBlank()) {
                                            versionStr += "-$release"
                                        }

                                        toolInstanceType = ToolInstanceType.WindowsSDK
                                        baseVersion = Version.parse(versionStr)
                                        platform = Platform.Default
                                        key = "${minorKey}_${regKey.bitness.platform.id}"

                                    }
                                }

                                // .NET Framework SDK
                                DotnetFrameworkSdkRegex.matchEntire(minorKey)?.groupValues?.let { values ->
                                    toolInstanceType = ToolInstanceType.DotNetFrameworkSDK
                                    baseVersion = Version.parse("${values[1]}${Version.Separator}${values[2]}")
                                    if(baseVersion.digits == 2 || baseVersion.digits == 3) {
                                        platform = Platform.tryParse(values[3]) ?: Platform.x86
                                        key = "${majorKey}_${baseVersion}_$platform"
                                    }
                                }

                                if (key != null && baseVersion != Version.Empty) {
                                    when (name) {
                                        InstallationFolderName -> {
                                            sdks.getOrPut(key!!) { Sdk(toolInstanceType, platform, baseVersion) }.installationFolder = File(value.text)
                                        }

                                        ProductVersionName -> {
                                            sdks.getOrPut(key!!) { Sdk(toolInstanceType, platform, baseVersion) }.detailedVersion = Version.parse(value.text.replace("..", "."))
                                        }
                                    }
                                }
                            }
                            return true
                        }

                    },
                    true)
        }

        return sdks
                .values
                .filter { it.installationFolder != null && it.detailedVersion != Version.Empty }
                .filter { it.toolInstanceType != ToolInstanceType.DotNetFrameworkSDK || _sdkInstanceFactory.tryCreate(it.installationFolder!!, it.baseVersion, it.platform) != null }
                .groupBy { it }
                .asSequence()
                .mapNotNull {
                    grp ->
                    grp.value.maxByOrNull { tool ->
                        tool.detailedVersion
                    }?.let {
                        max ->
                        ToolInstance(max.toolInstanceType, max.installationFolder!!, max.detailedVersion, max.baseVersion, max.platform)
                    }
                }
                .distinct()
                .toList()
    }


    companion object {
        private val LOG = Logger.getLogger(SdkRegistryProvider::class.java)
        private val DotnetFrameworkSdkRegex = Regex("^WinSDK-NetFx(\\d)(\\d)Tools-(x86|x64)\$", RegexOption.IGNORE_CASE)
        private val WinSdkVersionRegex = Regex("^v(\\d+\\.\\d+)(\\w*)\$", RegexOption.IGNORE_CASE)
        private const val InstallationFolderName = "installationfolder"
        private const val ProductVersionName = "productversion"

        internal val RegKeys = WindowsRegistryBitness.values().map {
            WindowsRegistryKey.create(
                    it,
                    WindowsRegistryHive.LOCAL_MACHINE,
                    "SOFTWARE",
                    "Microsoft",
                    "Microsoft SDKs")
        }
    }

    private class Sdk(
            val toolInstanceType: ToolInstanceType,
            val platform: Platform,
            val baseVersion: Version,
            var installationFolder: File? = null,
            var detailedVersion: Version = Version.Empty) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Sdk

            if (toolInstanceType != other.toolInstanceType) return false
            if (platform != other.platform) return false
            if (baseVersion != other.baseVersion) return false

            return true
        }

        override fun hashCode(): Int {
            var result = toolInstanceType.hashCode()
            result = 31 * result + platform.hashCode()
            result = 31 * result + baseVersion.hashCode()
            return result
        }
    }
}