package jetbrains.buildServer.agent

interface WindowsRegistry {
    fun accept(key: WindowsRegistryKey, visitor: WindowsRegistryVisitor, recursively: Boolean)
}