package jetbrains.buildServer.nunit

class NUnitBean {

    val nUnitPathKey: String
        get() = NUnitRunnerConstants.NUNIT_PATH

    val nUnitToolNameKey: String
        get() = NUnitRunnerConstants.NUNIT_TOOL_TYPE_ID

    val nUnitCommandLineKey: String
        get() = NUnitRunnerConstants.NUNIT_COMMAND_LINE

    val nUnitConfigFileKey: String
        get() = NUnitRunnerConstants.NUNIT_APP_CONFIG_FILE

    val nUnitIncludeKey: String
        get() = NUnitRunnerConstants.NUNIT_TESTS_FILES_INCLUDE

    val nUnitExcludeKey: String
        get() = NUnitRunnerConstants.NUNIT_TESTS_FILES_EXCLUDE

    val nUnitCategoryIncludeKey: String
        get() = NUnitRunnerConstants.NUNIT_CATEGORY_INCLUDE

    val nUnitCategoryExcludeKey: String
        get() = NUnitRunnerConstants.NUNIT_CATEGORY_EXCLUDE
}
