package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.agent.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.File

class VisualStudioRegistryProvider(
        private val _windowsRegistry: WindowsRegistry,
        private val _visualStudioInstanceFactory: ToolInstanceFactory,
        private val _visualStudioTestConsoleInstanceFactory: ToolInstanceFactory,
        private val _msTestConsoleInstanceFactory: ToolInstanceFactory)
    : ToolInstanceProvider {
    @Cacheable("ListOfVisualStuioAndTestFromRegistry", sync = true)
    override fun getInstances(): Collection<ToolInstance> {
        val instances = mutableSetOf<ToolInstance>()
        _windowsRegistry.accept(
                RegKey,
                object : WindowsRegistryVisitor {
                    override fun visit(key: WindowsRegistryKey): Boolean {
                        val version = Version.parse(key.parts.last())
                        if (version != Version.Empty && version.digits == 2) {
                            val visitor = this;
                            _windowsRegistry.accept(key, visitor, false)
                            _windowsRegistry.accept(key + "EnterpriseTools" + "QualityTools", visitor, false)
                        }

                        return true
                    }
                    override fun visit(value: WindowsRegistryValue): Boolean {
                        val parts = value.key.parts.takeLast(2)
                        if (
                                value.type == WindowsRegistryValueType.Str
                                && value.text.isNotBlank()
                                && "InstallDir".equals(parts[1], true)) {
                            if ("QualityTools".equals(parts[0], true)) {
                                _visualStudioTestConsoleInstanceFactory.tryCreate(File(value.text), Version.Empty, Platform.Default)?.let {
                                    LOG.debug("Found $it")
                                    instances.add(it)
                                }
                                _msTestConsoleInstanceFactory.tryCreate(File(value.text), Version.Empty, Platform.Default)?.let {
                                    LOG.debug("Found $it")
                                    instances.add(it)
                                }
                            }
                            else {
                                var baseVersion = Version.parse(parts[0])
                                if (baseVersion == Version.Empty || baseVersion.digits < 2
                                ) {
                                    LOG.debug("Cannot parse version from ${parts[0]}")
                                } else {
                                    _visualStudioInstanceFactory.tryCreate(File(value.text), baseVersion, Platform.Default)?.let {
                                        LOG.debug("Found $it")
                                        instances.add(it)
                                    }
                                }
                            }
                        }

                        return true
                    }
                },
                false)

        return instances.toList()
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