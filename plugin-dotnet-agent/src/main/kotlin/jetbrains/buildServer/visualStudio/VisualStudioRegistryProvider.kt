package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Version
import org.apache.log4j.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.File

class VisualStudioRegistryProvider(
        private val _windowsRegistry: WindowsRegistry,
        private val _visualStudioInstanceFactory: VisualStudioInstanceFactory)
    : VisualStudioProvider {
    @Cacheable("ListOfVisualStuioFromRegistry")
    override fun getInstances(): Sequence<VisualStudioInstance> {
        val instances = mutableSetOf<VisualStudioInstance>()
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
                            var version = Version.parse(parts[0])
                            if (version == Version.Empty) {
                                LOG.debug("Cannot parse version from ${parts[0]}")
                            }
                            else {
                                _visualStudioInstanceFactory.tryCreate(File(value.text), version)?.let {
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