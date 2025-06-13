package jetbrains.buildServer.nunit

object NUnitRunnerConstants {
    const val NUNIT_TOOL_TYPE_ID = "NUnit.Console"
    const val NUNIT_TOOL_TYPE_NAME = "NUnit Console"
    const val NUNIT_PACKAGE_ID = "NUnit.Console"
    const val NUNIT_RUN_TYPE = "nunit-console"

    const val MIN_NUPKG_VERSION = "4.0.0"

    // runner parameters
    const val NUNIT_TESTS_FILES_INCLUDE = "includeTests"
    const val NUNIT_TESTS_FILES_EXCLUDE = "excludeTests"
    const val NUNIT_CATEGORY_INCLUDE = "includeCategories"
    const val NUNIT_CATEGORY_EXCLUDE = "excludeCategories"
    const val NUNIT_PATH = "toolPath"
    const val NUNIT_COMMAND_LINE = "arguments"
    const val NUNIT_APP_CONFIG_FILE = "configFile"

    // configuration parameters
    // true or false  (false by default)
    const val NUNIT_USES_PROJECT_FILE = "teamcity.internal.dotnet.nunit.useProjectFile"
    // true or false (false by default)
    // always overwrite the listener in the tool's directory every time if it's already present
    const val OVERWRITE_TEAMCITY_EVENT_LISTENER = "teamcity.internal.dotnet.nunit.alwaysOverwriteTeamCityEventListener"

    // internal properties
    // true or false (true by default)
    const val NUNIT_RUNNER_ENABLED = "teamcity.internal.dotnet.nunit.runner.enabled"
}

