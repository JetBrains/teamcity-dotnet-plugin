

package jetbrains.buildServer.dotnet.test.dotnet.commands.msbuild

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameter
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameterConverterImpl
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameterType
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildParameterConverterTest {
    @MockK
    private lateinit var _parameterServiceMock: ParametersService

    @MockK
    private lateinit var _environment: Environment

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `should convert only if non-empty parameter name and non-empty parameter value are presented`() {
        // arrange
        every { _parameterServiceMock.tryGetParameter(any(), any()) } answers { null }
        every { _environment.os } answers { OSType.UNIX }
        val converter = MSBuildParameterConverterImpl(_parameterServiceMock, _environment)

        // act
        val actualParams = converter.convert(
            sequenceOf(
                MSBuildParameter("d", "   "),
                MSBuildParameter("d", ""),
                MSBuildParameter("a", "b"),
                MSBuildParameter("", "�"),
                MSBuildParameter("  ", "c")
            )
        ).toList()

        // assert
        Assert.assertEquals(actualParams, listOf("-p:a=b"))
    }

    @DataProvider
    fun casesForEscaping(): Array<Array<Any>> = arrayOf(
        // should escape all parameters
        arrayOf(
            sequenceOf(
                MSBuildParameter("comma", "a,b,c", MSBuildParameterType.Predefined),
                MSBuildParameter("comma", "b,c,a", MSBuildParameterType.Unknown),
                MSBuildParameter("semicolon", "a;b;c", MSBuildParameterType.Predefined),
                MSBuildParameter("semicolon", "b;c;a", MSBuildParameterType.Unknown),
            ),
            "true",
            listOf(
                "-p:comma=\"a%2Cb%2Cc\"",
                "-p:comma=\"b%2Cc%2Ca\"",
                "-p:semicolon=\"a%3Bb%3Bc\"",
                "-p:semicolon=\"b%3Bc%3Ba\""
            )
        ),

        // should escape only predefined parameters
        arrayOf(
            sequenceOf(
                MSBuildParameter("comma", "a,b,c", MSBuildParameterType.Predefined),
                MSBuildParameter("comma", "b,c,a", MSBuildParameterType.Unknown),
                MSBuildParameter("semicolon", "a;b;c", MSBuildParameterType.Predefined),
                MSBuildParameter("semicolon", "b;c;a", MSBuildParameterType.Unknown),
            ),
            "  ",
            listOf("-p:comma=\"a%2Cb%2Cc\"", "-p:comma=b,c,a", "-p:semicolon=\"a%3Bb%3Bc\"", "-p:semicolon=\"b;c;a\"")
        ),
    )

    @Test(dataProvider = "casesForEscaping")
    fun `should convert all parameters when escaping option in enabled`(
        parameters: Sequence<MSBuildParameter>,
        allParamsEscapingEnabled: String,
        expected: List<String>,
    ) {
        // arrange
        every {
            _parameterServiceMock.tryGetParameter(
                any(),
                DotnetConstants.PARAM_MSBUILD_PARAMETERS_ESCAPE
            )
        } answers { allParamsEscapingEnabled }
        every {
            _parameterServiceMock.tryGetParameter(
                any(),
                DotnetConstants.PARAM_MSBUILD_DISABLE_TRAILING_BACKSLASH_QUOTATION
            )
        } answers { "true" }
        every { _environment.os } answers { OSType.UNIX }
        val converter = MSBuildParameterConverterImpl(_parameterServiceMock, _environment)

        // act
        val actualParams = converter.convert(parameters).toList()

        // assert
        Assert.assertEquals(actualParams, expected)
    }

    @DataProvider
    fun casesForTrailingSlashQuotation(): Array<Array<Any>> = arrayOf(
        // feature off - escape only with special characters
        arrayOf("value 123\\", OSType.UNIX, "-p:name=\"value 123\\\\\"", "true"),
        arrayOf("value 123\\", OSType.WINDOWS, "-p:name=\"value 123\\\\\"", "true"),
        arrayOf("value123\\", OSType.UNIX, "-p:name=value123\\\\", "true"),
        arrayOf("value123\\", OSType.WINDOWS, "-p:name=value123\\\\", "true"),

        // feature on - escape one trailing slash for Windows and with special characters
        arrayOf("value 123\\", OSType.UNIX, "-p:name=\"value 123\\\\\"", "false"),
        arrayOf("value 123\\", OSType.WINDOWS, "-p:name=\"value 123\\\\\"", "false"),
        arrayOf("value123\\", OSType.UNIX, "-p:name=value123\\\\", "false"),
        arrayOf("value123\\", OSType.WINDOWS, "-p:name=\"value123\\\\\"", "false"),
    )

    @Test(dataProvider = "casesForTrailingSlashQuotation")
    fun `should convert value when escaping trailing backslash`(
        value: String,
        osType: OSType,
        expectedValue: String,
        isTrailingBackslashQuotationDisabled: String
    ) {
        // arrange
        every {
            _parameterServiceMock.tryGetParameter(
                any(),
                DotnetConstants.PARAM_MSBUILD_DISABLE_TRAILING_BACKSLASH_QUOTATION
            )
        } answers { isTrailingBackslashQuotationDisabled }
        every {
            _parameterServiceMock.tryGetParameter(
                any(),
                DotnetConstants.PARAM_MSBUILD_PARAMETERS_ESCAPE
            )
        } answers { "false" }
        every { _environment.os } answers { osType }
        val converter = MSBuildParameterConverterImpl(_parameterServiceMock, _environment)
        val params = sequenceOf(MSBuildParameter("name", value, MSBuildParameterType.Unknown))

        // act
        val actualValue = converter.convert(params).toList()[0]

        // assert
        Assert.assertEquals(actualValue, expectedValue)
    }
}