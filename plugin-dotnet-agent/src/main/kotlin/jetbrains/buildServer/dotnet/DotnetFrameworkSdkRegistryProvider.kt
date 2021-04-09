package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.agent.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.File

class DotnetFrameworkSdkRegistryProvider(
        private val _windowsRegistry: WindowsRegistry,
        private val _sdkInstanceFactory: ToolInstanceFactory)
    : ToolInstanceProvider {
    @Cacheable("ListOfDotnetFrameworkSdkFromRegistry", sync = true)
    override fun getInstances(): Collection<ToolInstance> {
            val sdks = mutableListOf<ToolInstance>()
            _windowsRegistry.accept(
                    RegKey,
                    object : WindowsRegistryVisitor {
                        override fun visit(key: WindowsRegistryKey) = false
                        override fun visit(value: WindowsRegistryValue): Boolean {
                            val name = value.key.parts.last()
                            if (
                                    value.type == WindowsRegistryValueType.Str
                                    && value.text.isNotBlank()
                                    && name.startsWith(VersionPrefix, true)) {
                                val version = Version.parse(name.substring(VersionPrefix.length))
                                if (version != Version.Empty) {
                                    if (version.digits == 2) {
                                        _sdkInstanceFactory.tryCreate(File(value.text), version, Platform.x86)?.let {
                                            LOG.debug("Found $it");
                                            sdks.add(it)
                                        }
                                    }
                                }
                                else {
                                    LOG.debug("Cannot parse version from $name")
                                }
                            }
                            return true
                        }

                    },
                    false)

            return sdks
        }

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworkSdkRegistryProvider::class.java)
        private val VersionPrefix = "sdkInstallRootv"

        internal val RegKey = WindowsRegistryKey.create(
                WindowsRegistryBitness.Bitness32,
                WindowsRegistryHive.LOCAL_MACHINE,
                "SOFTWARE",
                "Microsoft",
                ".NETFramework")
    }
}