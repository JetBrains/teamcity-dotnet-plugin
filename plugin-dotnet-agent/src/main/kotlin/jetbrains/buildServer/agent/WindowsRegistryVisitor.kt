package jetbrains.buildServer.agent

interface WindowsRegistryVisitor {
    fun visit(key: WindowsRegistryKey): Boolean

    fun visit(value: WindowsRegistryValue): Boolean
}