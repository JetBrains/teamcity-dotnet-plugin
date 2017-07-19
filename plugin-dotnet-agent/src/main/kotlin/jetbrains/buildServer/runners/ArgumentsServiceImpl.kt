package jetbrains.buildServer.runners

class ArgumentsServiceImpl : ArgumentsService {
    override fun parseToStrings(text: String)  =
            jetbrains.buildServer.util.StringUtil.splitCommandArgumentsAndUnquote(text)
                    .asSequence()
                    .filter { !it.isNullOrBlank() }
}