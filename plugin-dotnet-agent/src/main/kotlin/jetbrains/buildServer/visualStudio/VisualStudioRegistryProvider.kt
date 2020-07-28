package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.Platform
import org.apache.log4j.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.File

class VisualStudioRegistryProvider(
        private val _windowsRegistry: WindowsRegistry,
        private val _visualStudioInstanceFactory: ToolInstanceFactory)
    : ToolInstanceProvider {
    @Cacheable("ListOfVisualStuioFromRegistry")
    override fun getInstances(): Sequence<ToolInstance> {
        val instances = mutableSetOf<ToolInstance>()
        _windowsRegistry.get(
                RegKey,
                object : WindowsRegistryVisitor {
                    override fun accept(key: WindowsRegistryKey): Boolean {
                        val version = Version.parse(key.parts.last())
                        if (version != Version.Empty && version.digits == 2) {
                            _windowsRegistry.get(key, this, false)
                        }

                        return true
                    }
                    override fun accept(value: WindowsRegistryValue): Boolean {
                        val parts = value.key.parts.takeLast(2)
                        if (
                                value.type == WindowsRegistryValueType.Str
                                && value.text.isNotBlank()
                                && "InstallDir".equals(parts[1], true)) {
                            var baseVersion = Version.parse(parts[0])
                            if (baseVersion == Version.Empty) {
                                LOG.debug("Cannot parse version from ${parts[0]}")
                            }
                            else {
                                _visualStudioInstanceFactory.tryCreate(File(value.text), baseVersion, Platform.Default)?.let {
                                    LOG.debug("Found $it");
                                    instances.add(it)
                                }
                            }

                            return false
                        }

                        return true
                    }
                },
                false)

        return instances.asSequence()
    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioRegistryProvider::class.java)

        internal val RegKey = WindowsRegistryKey.create(
                        WindowsRegistryBitness.Bitness32,
                        WindowsRegistryHive.LOCAL_MACHINE,
                        "SOFTWARE",
                        "Microsoft",
                        "VisualStudio")
    }
}