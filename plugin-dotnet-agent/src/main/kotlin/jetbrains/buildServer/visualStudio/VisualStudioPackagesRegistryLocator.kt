package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger

class VisualStudioPackagesRegistryLocator(
        private val _windowsRegistry: WindowsRegistry)
    : VisualStudioPackagesLocator {

    override fun tryGetPackagesPath(): String? {
        var packagesPath: String? = null;
        for (key in RegKeys) {
            _windowsRegistry.accept(
                    key,
                    object : WindowsRegistryVisitor {
                        override fun visit(key: WindowsRegistryKey) = false
                        override fun visit(value: WindowsRegistryValue): Boolean {
                            if (value.type == WindowsRegistryValueType.Str && "CachePath".equals(value.key.parts.lastOrNull(), true)) {
                                packagesPath = value.text
                                LOG.debug("Using Visual Studio packages cache directory \"$packagesPath\"");
                                return false
                            }

                            return true
                        }
                    },
                    false)
        }

        return packagesPath
    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioPackagesRegistryLocator::class.java)

        internal val RegKeys =
                WindowsRegistryBitness
                        .values()
                        .asSequence()
                        .map {
                            WindowsRegistryKey.create(
                                    it,
                                    WindowsRegistryHive.LOCAL_MACHINE,
                                    "SOFTWARE",
                                    "Microsoft",
                                    "VisualStudio",
                                    "Setup")
                        }
    }
}