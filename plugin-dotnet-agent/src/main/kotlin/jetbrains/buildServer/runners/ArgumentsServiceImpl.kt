package jetbrains.buildServer.runners

import jetbrains.buildServer.util.StringUtil

class ArgumentsServiceImpl : ArgumentsService {

    override fun split(text: String) =
            StringUtil.splitCommandArgumentsAndUnquote(text)
                    .asSequence()
                    .filter { !it.isNullOrBlank() }

    override fun combine(arguments: Sequence<String>): String =
            arguments
                    .map { "${QUOTE_STR}${it}${QUOTE_STR}" }
                    .joinToString(ARGS_SEPARATOR);

    companion object {
        private val ARGS_SEPARATOR = " "
        private val QUOTE_STR = "\""
    }
}