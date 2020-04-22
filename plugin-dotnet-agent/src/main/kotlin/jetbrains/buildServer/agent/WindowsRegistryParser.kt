package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.WindowsRegistryKey
import jetbrains.buildServer.agent.WindowsRegistryValue

interface WindowsRegistryParser {
    fun tryParseKey(key: WindowsRegistryKey, text: String): WindowsRegistryKey?

    fun tryParseValue(key: WindowsRegistryKey, text: String): WindowsRegistryValue?
}