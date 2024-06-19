package jetbrains.buildServer.nunit.arguments

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.nunit.NUnitSettings
import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.util.regex.Pattern

class NUnitTestingAssembliesProvider(
    private val _nUnitSettings: NUnitSettings,
    private val _pathMatcher: PathMatcher,
    private val _loggerService: LoggerService,
    private val _pathsService: PathsService
) {
    val assemblies: List<File>
        get() {
            val assemblies = _pathMatcher.match(
                path = _pathsService.getPath(PathType.Checkout),
                includeRules = parseScanRules(_nUnitSettings.includeTestFiles),
                excludeRules = parseScanRules(_nUnitSettings.excludeTestFiles)
            ).map { assembly -> assembly.absoluteFile }

            if (assemblies.isEmpty()) {
                _loggerService.writeBuildProblem(
                    NO_ASSEMBLIES_WERE_FOUND_ID,
                    BuildProblemData.TC_ERROR_MESSAGE_TYPE, NO_ASSEMBLIES_WERE_FOUND_BUILD_PROBLEM
                )
            }

            return assemblies
        }

    private fun parseScanRules(rulesToParse: String?): List<String> {
        val rules = mutableListOf<String>()
        if (rulesToParse != null) {
            for (rule in DELIMITER.split(FileUtil.normalizeSeparator(rulesToParse))) {
                if (rule != null && rule.trim { it <= ' ' }.isNotEmpty()) {
                    rules.add(rule)
                }
            }
        }

        return rules
    }

    companion object {
        private val DELIMITER: Pattern = Pattern.compile("(,|\\n)")
        const val NO_ASSEMBLIES_WERE_FOUND_ID: String = "NO_TEST_ASSEMBLIES"
        const val NO_ASSEMBLIES_WERE_FOUND_BUILD_PROBLEM: String = "No assemblies were found."
    }
}