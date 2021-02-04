package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import org.apache.log4j.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.File

class DotnetFrameworksEnvironmentImpl(
        private val _windowsRegistry: WindowsRegistry)
    : DotnetFrameworksEnvironment {
    @Cacheable("tryGetRoot", key = "#bitness.platform.id")
    override fun tryGetRoot(bitness: WindowsRegistryBitness): File? {
        var root: File? = null
        _windowsRegistry.accept(
                WindowsRegistryKey.create(
                        bitness,
                        WindowsRegistryHive.LOCAL_MACHINE,
                        "SOFTWARE",
                        "Microsoft",
                        ".NETFramework"),

                object : WindowsRegistryVisitor {
                    override fun visit(key: WindowsRegistryKey) = true
                    override fun visit(value: WindowsRegistryValue): Boolean {
                        if (
                                value.type == WindowsRegistryValueType.Str
                                && value.text.isNotBlank()
                                && "InstallRoot".equals(value.key.parts.lastOrNull(), true)) {
                            root = File(value.text)
                            return false
                        }

                        return true
                    }
                },
                false
        )

        LOG.info(".NET Framework ${bitness.platform.id} install root: \"${root ?: "empty"}\"");
        return root
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworksEnvironmentImpl::class.java)
    }
}