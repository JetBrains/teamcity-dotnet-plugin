package jetbrains.buildServer.dotnet.discovery.dotnetFramework

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.File

class DotnetFrameworksEnvironmentImpl(
    private val _windowsRegistry: WindowsRegistry,
) : DotnetFrameworksEnvironment {
    @Cacheable("tryGetRoot", key = "#bitness + '_' + #isArm", sync = true)
    override fun tryGetRoot(bitness: WindowsRegistryBitness, isArm: Boolean): File? {
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
                        && getRegistryKey(isArm).equals(value.key.parts.lastOrNull(), true)
                    ) {
                        root = File(value.text)
                        return false
                    }

                    return true
                }
            },
            false
        )

        LOG.debug(".NET Framework ${bitness.getPlatform(isArm).id} install root: \"${root ?: "empty"}\"");
        return root
    }

    private fun getRegistryKey(isArm: Boolean) = when (isArm) {
        true -> "InstallRootArm64"
        false -> "InstallRoot"
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworksEnvironmentImpl::class.java)
    }
}