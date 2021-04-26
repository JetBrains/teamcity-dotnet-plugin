package jetbrains.buildServer.inspect

import jetbrains.buildServer.E

interface PluginSource {
    val id: String

    fun getPlugin(specification: String): E
}