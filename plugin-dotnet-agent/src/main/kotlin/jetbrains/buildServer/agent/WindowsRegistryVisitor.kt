package jetbrains.buildServer.agent

interface WindowsRegistryVisitor {
    fun accept(key: WindowsRegistryKey)

    fun accept(value: WindowsRegistryValue)
}