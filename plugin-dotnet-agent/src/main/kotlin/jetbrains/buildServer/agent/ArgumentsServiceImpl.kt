package jetbrains.buildServer.agent

import jetbrains.buildServer.util.StringUtil

class ArgumentsServiceImpl : ArgumentsService {
    override fun normalize(argument: String) =
            when {
                argument.trim().length == 0 -> argument
                argument.contains(ArgumentsService.ArgsSeparator) -> StringUtil.doubleQuote(StringUtil.unquoteString(argument))
                else -> argument
            }

    override fun split(text: String) =
            StringUtil.splitCommandArgumentsAndUnquote(text)
                    .asSequence()
                    .filter { !it.isNullOrBlank() }

    override fun combine(arguments: Sequence<String>, argumentsSeparator: String): String =
            arguments.map { normalize(it) }.joinToString(argumentsSeparator)
}