package jetbrains.buildServer.agent

import jetbrains.buildServer.util.StringUtil

class ArgumentsServiceImpl : ArgumentsService {

    override fun split(text: String) =
            StringUtil.splitCommandArgumentsAndUnquote(text)
                    .asSequence()
                    .filter { !it.isNullOrBlank() }

    override fun combine(arguments: Sequence<String>): String =
            arguments
                    .map {
                        if (it.contains(ARGS_SEPARATOR)) {
                            "${QUOTE_STR}${it}${QUOTE_STR}"
                        }
                        else {
                            it
                        }
                    }
                    .joinToString(ARGS_SEPARATOR);

    override fun escape(text: String): String {
        return StringUtil.escapeStringCharacters(text)
    }

    companion object {
        private val ARGS_SEPARATOR = " "
        private val QUOTE_STR = "\""
    }
}