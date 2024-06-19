package jetbrains.buildServer.nunit.toolState

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.nunit.NUnitSettings
import jetbrains.buildServer.util.VersionComparatorUtil

class NUnitToolStateVerifier(
    private val _loggerService: LoggerService,
    private val _nUnitSettings: NUnitSettings
) {
    fun verify(info: NUnitToolState) {
        if (VersionComparatorUtil.compare(
                info.nUnitVersion,
                NUNIT_VERSION_SUPPORTING_EXTENSIONS
            ) >= 0
        ) {
            val notFoundExtensions = mutableListOf<String>()
            for (requiredExtension in requiredExtensions) {
                if (!info.extensions.contains(requiredExtension)) {
                    notFoundExtensions.add(requiredExtension)
                }
            }

            if (notFoundExtensions.size == 0) {
                return
            }

            val errorMessage = buildString {
                append("The TeamCity NUnit runner requires the following NUnit extensions to be installed: ")
                append(notFoundExtensions.joinToString(", "))
                append(". Please follow our instructions at: $HELP_URL")
            }
            _loggerService.writeErrorOutput(errorMessage)

        }
    }

    private val requiredExtensions
        get() = sequence {
            if (_nUnitSettings.useProjectFile) {
                yield("NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader")
            }

            yield("NUnit.Engine.Listeners.TeamCityEventListener")

        }

    companion object {
        private const val HELP_URL: String = "https://www.jetbrains.com/help/teamcity/?NUnit#NUnit-NUnit3Extensions"
        private const val NUNIT_VERSION_SUPPORTING_EXTENSIONS = "3.4.1"
    }
}
