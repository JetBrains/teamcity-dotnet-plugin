package jetbrains.buildServer.agent

interface WindowsRegistry {
    fun get(key: WindowsRegistryKey, visitor: WindowsRegistryVisitor, recursively: Boolean)
}