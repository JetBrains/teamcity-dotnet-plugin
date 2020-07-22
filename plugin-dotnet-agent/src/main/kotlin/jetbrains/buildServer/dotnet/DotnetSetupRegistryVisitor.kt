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
                var version = framework.version
                if (version == Version.Empty) {
                    version = framework.release
                }

                if (version != Version.Empty) {
                    yield(DotnetFramework(framework.bitness.platform, version, frameworkBasePath))
                }
            }
        }

        _frameworks.clear()
    }

    override fun accept(key: WindowsRegistryKey) = true

    override fun accept(value: WindowsRegistryValue): Boolean {
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
                            framework.release = FrameworkVersions[value.number] ?: Version.Empty
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

        private val FrameworkVersions = mapOf(
                378389L to Version(4, 5),
                378675L to Version(4, 5, 1),
                378758L to Version(4, 5, 1),
                379893L to Version(4, 5, 2),
                393295L to Version(4, 6),
                393297L to Version(4, 6),
                394254L to Version(4, 6, 1),
                394271L to Version(4, 6, 1),
                394802L to Version(4, 6, 2),
                394806L to Version(4, 6, 2),
                460798L to Version(4, 7),
                460805L to Version(4, 7),
                461308L to Version(4, 7, 1),
                461310L to Version(4, 7, 1),
                461808L to Version(4, 7, 2),
                461814L to Version(4, 7, 2),
                528040L to Version(4, 8),
                528049L to Version(4, 8),
                528372L to Version(4, 8))
    }

    private data class Framework(
            val bitness: WindowsRegistryBitness,
            var version: Version = Version.Empty,
            var release: Version = Version.Empty,
            var path: File? = null) {
        val isEmpty get() = !((version != Version.Empty || release != Version.Empty) && path != null)
    }
}