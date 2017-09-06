package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetLoggerImpl
import jetbrains.buildServer.dotnet.Logger
import jetbrains.buildServer.runners.FileSystemService
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetLoggerTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // INTEGRATION_PACKAGE_HOME runner parameter is not specified
                arrayOf(
                        Logger.MSBuildLogger15,
                        VirtualFileSystemService(),
                        emptyMap<String, String>(),
                        null,
                        null),

                // Success scenario
                arrayOf(
                        Logger.MSBuildLogger15,
                        VirtualFileSystemService().addFile(File(File("home", "msbuild15"), "TeamCity.MSBuild.Logger.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home"),
                        File(File("home", "msbuild15"), "TeamCity.MSBuild.Logger.dll"),
                        null),

                // Has no assembly
                arrayOf(
                        Logger.MSBuildLogger15,
                        VirtualFileSystemService().addDirectory(File("home", "msbuild15")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home"),
                        null,
                        "Path \".+\" to logger was not found" as String?),

                // Has no directory
                arrayOf(
                        Logger.MSBuildLogger15,
                        VirtualFileSystemService(),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home"),
                        null,
                        "Path \".+\" to integration pack was not found" as String?)
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            logger: Logger,
            fileSystemService: FileSystemService,
            parameters: Map<String, String>,
            expectedLogger: File?,
            expectedErrorPattern: String?) {
        // Given
        val loggerProvider = DotnetLoggerImpl(ParametersServiceStub(parameters), fileSystemService)

        // When
        var actualLogger: File? = null;
        try {
            actualLogger = loggerProvider.tryGetToolPath(logger)
        }
        catch (ex: RunBuildException) {
            if(expectedErrorPattern != null) {
                Assert.assertEquals(Regex(expectedErrorPattern).matches(ex.message as String), true)
            }
            else {
                Assert.fail("Unexpected exception ${ex}")
            }
        }

        // Then
        if(actualLogger != null) {
            Assert.assertEquals(actualLogger, expectedLogger)
        }
    }
}