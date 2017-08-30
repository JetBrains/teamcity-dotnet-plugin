package jetbrains.buildServer.runners

interface ArgumentsService {
    fun split(text: String): Sequence<String>

    fun combine(arguments: Sequence<String>): String
}