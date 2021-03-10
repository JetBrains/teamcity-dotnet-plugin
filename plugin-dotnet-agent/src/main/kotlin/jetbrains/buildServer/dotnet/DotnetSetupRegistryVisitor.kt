package jetbrains.buildServer.dotnet

import WindowsRegistryValueType
import jetbrains.buildServer.agent.*
import java.io.File

class DotnetSetupRegistryVisitor(
        private val _environment: DotnetFrameworksEnvironment)
    : DotnetFrameworksWindowsRegistryVisitor {

    private val _frameworks = mutableMapOf<String, Framework>()

    override val keys = Keys

    override fun getFrameworks() = sequence {
        for (framework in _frameworks.values.filter { !it.isEmpty }) {
            val frameworkBasePath = framework.path
            if (frameworkBasePath != null) {
                var version: Version
                if (framework.release.major == framework.version.major && framework.release.minor == framework.version.minor && (framework.release.patch == framework.version.patch || framework.release.patch == 0)) {
                    version = framework.version
                }
                else {
                    version = framework.release
                    if (version == Version.Empty) {
                        version = framework.version
                    }
                }

                if (version != Version.Empty) {
                    yield(DotnetFramework(framework.bitness.platform, version, frameworkBasePath))
                }
            }
        }

        _frameworks.clear()
    }

    override fun visit(key: WindowsRegistryKey) = true

    override fun visit(value: WindowsRegistryValue): Boolean {
        val bitness = value.key.bitness
        if (value.key.parts.size == Deep + 2) {
            val subKey = value.key.parts.takeLast(2)
            val key = subKey[0].toLowerCase()
            val name = subKey[1].toLowerCase()
            if (value.type == WindowsRegistryValueType.Str && value.text.isNotBlank() && key.startsWith("v")) {
                when (key) {
                    "v3.0" -> {
                        when (name) {
                            VersionName -> {
                                val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                                framework.version = Version.parse(value.text)
                                _environment.tryGetRoot(value.key.bitness)?.let {
                                    installRoot -> framework.path = File(installRoot, key)
                                }
                            }
                        }
                    }

                    "v3.5" -> {
                        when (name) {
                            VersionName -> {
                                val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                                framework.version = Version.parse(value.text)
                                if (framework.path == null) {
                                    _environment.tryGetRoot(value.key.bitness)?.let {
                                        installRoot -> framework.path = File(installRoot, key)
                                    }
                                }
                            }
                            InstallPathName -> {
                                val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                                framework.path = if (value.text.isNotBlank()) File(value.text) else null
                            }
                        }
                    }
                }
            }
        }

        if (value.key.parts.size == Deep + 3) {
            val subKey = value.key.parts.takeLast(3)
            val key = subKey[0].toLowerCase()
            val versionType = subKey[1]
            val name = subKey[2]
            if (key.equals("v4") && versionType.equals("Full", true)) {
                when (name.toLowerCase()) {
                    VersionName -> {
                        if (value.type == WindowsRegistryValueType.Str && value.text.isNotBlank()) {
                            val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                            framework.version = Version.parse(value.text)
                        }
                    }
                    InstallPathName -> {
                        if (value.type == WindowsRegistryValueType.Str && value.text.isNotBlank()) {
                            val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                            framework.path = if (value.text.isNotBlank()) File(value.text) else null
                        }
                    }
                    ReleaseName -> {
                        if (value.type == WindowsRegistryValueType.Int && value.number != 0L) {
                            val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                            framework.release = getFrameworkVersion(value.number)
                        }
                    }
                }
            }
        }

        return true
    }

    companion object {
        val InstallPathName = "installpath"
        val VersionName = "version"
        val ReleaseName = "release"

        val Keys: Sequence<WindowsRegistryKey> get()  =
            WindowsRegistryBitness
                    .values()
                    .asSequence()
                    .map {
                        WindowsRegistryKey.create(
                                it,
                                WindowsRegistryHive.LOCAL_MACHINE,
                                "SOFTWARE",
                                "Microsoft",
                                "NET Framework Setup",
                                "NDP")
                    }

        private val Deep = DotnetPolicyRegistryVisitor.Keys.first().parts.size

        private fun getFrameworkVersion(releaseKey: Long) : Version {
            if (releaseKey >= 528040)
                return Version(4, 8)

            if (releaseKey >= 461808)
                return Version(4, 7, 2)

            if (releaseKey >= 461308)
                return Version(4, 7, 1)

            if (releaseKey >= 460798)
                return Version(4, 7)

            if (releaseKey >= 394802)
                return Version(4, 6, 2)

            if (releaseKey >= 394254)
                return Version(4, 6, 1)

            if (releaseKey >= 393295)
                return Version(4, 6)

            if (releaseKey >= 379893)
                return Version(4, 5, 2)

            if (releaseKey >= 378675)
                return Version(4, 5, 1)

            if (releaseKey >= 378389)
                return Version(4, 5)

            return Version.Empty
        }
    }

    private data class Framework(
            val bitness: WindowsRegistryBitness,
            var version: Version = Version.Empty,
            var release: Version = Version.Empty,
            var path: File? = null) {
        val isEmpty get() = !((version != Version.Empty || release != Version.Empty) && path != null)
    }
}