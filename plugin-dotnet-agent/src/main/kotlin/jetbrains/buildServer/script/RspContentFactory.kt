package jetbrains.buildServer.script

interface RspContentFactory {
    fun create() : Sequence<String>
}