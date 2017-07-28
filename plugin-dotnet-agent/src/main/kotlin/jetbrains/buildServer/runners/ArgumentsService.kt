package jetbrains.buildServer.runners

interface ArgumentsService {
    fun parseToStrings(text: String): Sequence<String>
}