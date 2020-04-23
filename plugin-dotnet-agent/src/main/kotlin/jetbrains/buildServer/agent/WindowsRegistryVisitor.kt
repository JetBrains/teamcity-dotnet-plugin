package jetbrains.buildServer.agent

interface WindowsRegistryVisitor {
    fun accept(key: WindowsRegistryKey): Boolean

    fun accept(value: WindowsRegistryValue): Boolean
}