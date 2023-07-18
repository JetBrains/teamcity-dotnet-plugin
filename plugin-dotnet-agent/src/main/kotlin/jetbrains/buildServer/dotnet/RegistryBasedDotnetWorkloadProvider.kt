package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.WindowsRegistryHive.LOCAL_MACHINE
import jetbrains.buildServer.dotnet.DotnetWorkloadProviderBase.SourceType.WINDOWS_REGISTRY
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import jetbrains.buildServer.util.OSType.WINDOWS
import org.springframework.cache.annotation.Cacheable
import java.io.File

open class RegistryBasedDotnetWorkloadProvider(
    private val _environment: Environment,
    private val _windowsRegistry: WindowsRegistry,
    sdksProvider: DotnetSdksProvider,
    versionEnumerator: VersionEnumerator,
) : DotnetWorkloadProviderBase(sdksProvider, versionEnumerator) {

    override fun source(dotnetExecutable: File) = Source(
        type = WINDOWS_REGISTRY,
        path = RegKeys[0].regKey
    )

    @Cacheable("ListOfDotNetWorkloadsFromRegistry", sync = true)
    override fun getRawWorkloadsInfo(dotnetExecutable: File): Map<Version, List<String>> {
        if (_environment.os != WINDOWS)
            return emptyMap()

        LOG.debug("Trying to get the list of installed workloads for all .NET SDKs from the Windows Registry")
        val workloads = mutableMapOf<Version, MutableSet<String>>()
        val seenKeys = mutableSetOf<String>()
        RegKeys.forEach { regKey ->
            _windowsRegistry.accept(
                regKey,
                object : WindowsRegistryVisitor {
                    override fun visit(key: WindowsRegistryKey): Boolean {
                        if (seenKeys.contains(key.regKey) || key.parts.size > EXPECTED_DEPTH || key.parts.size < MIN_DEPTH)
                            return true
                        seenKeys.add(key.regKey)

                        val keyName = key.parts.last()
                        if (isPlatformValid(keyName) || Version.isValid(keyName)) {
                            val visitor = this
                            _windowsRegistry.accept(key, visitor, false)
                        }

                        val keys = key.parts.takeLast(3)
                        val platform = keys[0]
                        val version = keys[1]
                        val workload = keys[2]

                        if (isPlatformValid(platform) && Version.isValid(version) && key.parts.size == EXPECTED_DEPTH && workload.isNotEmpty())
                            workloads.computeIfAbsent(Version.parse(version)) { mutableSetOf() }.add(workload)

                        return true
                    }

                    override fun visit(value: WindowsRegistryValue): Boolean = true
                },
                false
            )
        }

        return workloads.map { it.key to it.value.toList() }.toMap()
    }

    private fun isPlatformValid(platform: String) =
        platform == Platform.x86.id || platform == Platform.x64.id ||
                platform.startsWith(Platform.ARM.id, ignoreCase = true)

    companion object {
        private val LOG = Logger.getLogger(RegistryBasedDotnetWorkloadProvider::class.java)
        val RegKeys = WindowsRegistryBitness.values().map {
            WindowsRegistryKey.create(
                it,
                LOCAL_MACHINE,
                "SOFTWARE",
                "Microsoft",
                "dotnet",
                "InstalledWorkloads",
                "Standalone"
            )
        }
        const val EXPECTED_DEPTH = 8
        const val MIN_DEPTH = 5
    }
}