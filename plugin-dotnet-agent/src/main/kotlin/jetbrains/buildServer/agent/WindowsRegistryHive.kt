package jetbrains.buildServer.agent

enum class WindowsRegistryHive(val rootKey: String) {
    LOCAL_MACHINE("HKEY_LOCAL_MACHINE"),
    CURRENT_USER("HKEY_CURRENT_USER"),
    CLASSES_ROOT("HKEY_CLASSES_ROOT")
}