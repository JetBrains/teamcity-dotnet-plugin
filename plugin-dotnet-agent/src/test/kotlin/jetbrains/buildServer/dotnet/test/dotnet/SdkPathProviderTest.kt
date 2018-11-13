package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.ToolProvidersRegistry
import jetbrains.buildServer.agent.ToolSearchService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.SdkPathProviderImpl
import jetbrains.buildServer.util.OSType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class SdkPathProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _toolProvider: ToolProvider
    private lateinit var _environment: Environment

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _toolProvider = _ctx.mock(ToolProvider::class.java)
        _environment = _ctx.mock(Environment::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.WINDOWS, File(File("winSdk"), "sdk")),
                arrayOf(OSType.UNIX, File(File("/usr/share/dotnet"), "sdk")),
                arrayOf(OSType.MAC, File(File("/usr/local/share/dotnet"), "sdk")))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideSdkPath(os: OSType, expectedPath: File) {
        // Given
        _ctx.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).os
                will(returnValue(os))

                allowing<ToolProvider>(_toolProvider).getPath(DotnetConstants.EXECUTABLE)
                will(returnValue(File(File("winSdk"), "dotnet.exe").path))
            }
        })
        val sdkPathProviderImpl = SdkPathProviderImpl(_toolProvider, _environment)

        // When
        val actualPath = sdkPathProviderImpl.path

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualPath, expectedPath)
    }
}