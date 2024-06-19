package jetbrains.buildServer.nunit.arguments

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.messages.serviceMessages.Message
import jetbrains.buildServer.nunit.NUnitSettings

class NUnitTestFilterProvider(
    private val _nUnitSettings: NUnitSettings,
    private val _argumentsService: ArgumentsService,
    private val _loggerService: LoggerService
) {
    val filter: String
        get() {
            val additionalCmdLine = _nUnitSettings.additionalCommandLine
            val whereArg = additionalCmdLine
                ?.let { _argumentsService.split(it).map { arg -> CommandLineArgument(arg) } }
                ?.firstOrNull { data -> "--where".equals(data.value, ignoreCase = true) }

            val includeCategories = parseCategories(_nUnitSettings.includeCategories)
            val excludeCategories = parseCategories(_nUnitSettings.excludeCategories)

            if (whereArg != null && (includeCategories.isNotEmpty() || excludeCategories.isNotEmpty())) {
                _loggerService.writeWarning(WARNING_MESSAGE)
                LOG.info(WARNING_MESSAGE)
                return ""
            }

            var includeFilter = includeCategories.joinToString(OR_KEYWORD) { "cat==$it" }
            val excludeFilter = excludeCategories.joinToString(AND_KEYWORD) { "cat!=$it" }

            includeFilter =
                if (includeCategories.size > 1 && excludeCategories.isNotEmpty()) "($includeFilter)" else includeFilter
            val catSeparator = if (includeCategories.isNotEmpty() && excludeCategories.isNotEmpty()) AND_KEYWORD else ""
            return includeFilter + catSeparator + excludeFilter
        }

    companion object {
        private const val CATEGORY_SEPARATOR = ","
        private const val SIMPLE_NEW_LINE = "\n"
        private const val OR_KEYWORD = "||"
        private const val AND_KEYWORD = "&&"
        private const val WARNING_MESSAGE: String =
            "NUnit categories include/exclude were ignored because the additional command line parameters contain the argument \"where\"."
        private val LOG = Logger.getInstance(NUnitTestFilterProvider::class.java.name)

        private fun parseCategories(categoriesStr: String?): List<String> {
            if (categoriesStr.isNullOrBlank()) {
                return emptyList()
            }

            return categoriesStr
                .replace(System.lineSeparator(), CATEGORY_SEPARATOR)
                .replace(SIMPLE_NEW_LINE, CATEGORY_SEPARATOR)
                .split(CATEGORY_SEPARATOR)
                .map { source -> source.trim { it <= ' ' } }
                .filter { it.isNotEmpty() }
        }
    }
}