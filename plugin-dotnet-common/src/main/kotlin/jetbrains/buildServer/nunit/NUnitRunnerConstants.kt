package jetbrains.buildServer.nunit

object NUnitRunnerConstants {
    const val NUNIT_TOOL_TYPE_ID = "NUnit.Console"
    const val NUNIT_TOOL_TYPE_NAME = "NUnit Console"
    const val NUNIT_PACKAGE_ID = "NUnit.Console"
    const val NUNIT_RUN_TYPE = "nunit-console"

    // runner parameters
    const val NUNIT_TESTS_FILES_INCLUDE = "includeTests"
    const val NUNIT_TESTS_FILES_EXCLUDE = "excludeTests"
    const val NUNIT_CATEGORY_INCLUDE = "includeCategories"
    const val NUNIT_CATEGORY_EXCLUDE = "excludeCategories"
    const val NUNIT_PATH = "toolPath"
    const val NUNIT_COMMAND_LINE = "arguments"
    const val NUNIT_APP_CONFIG_FILE = "configFile"

    // configuration parameter (false by default)
    const val NUNIT_USES_PROJECT_FILE = "nunit_use_project_file"

    // internal properties
    // true or false (false by default)
    const val NUNIT_RUNNER_ENABLED = "teamcity.internal.dotnet.nunit.runner.enabled"
}

