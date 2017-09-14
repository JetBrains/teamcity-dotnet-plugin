package jetbrains.buildServer.agent

interface ArgumentsService {
    fun split(text: String): Sequence<String>

    fun combine(arguments: Sequence<String>, argumentsSeparator: String = ArgsSeparator): String

    fun escape(text: String): String

    companion object {
        internal val ArgsSeparator = " "
    }
}