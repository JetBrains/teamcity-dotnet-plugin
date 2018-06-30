package jetbrains.buildServer.agent

interface ArgumentsService {
    fun split(text: String): Sequence<String>

    fun combine(arguments: Sequence<String>, argumentsSeparator: String = ArgsSeparator): String

    companion object {
        internal const val ArgsSeparator = " "
    }
}