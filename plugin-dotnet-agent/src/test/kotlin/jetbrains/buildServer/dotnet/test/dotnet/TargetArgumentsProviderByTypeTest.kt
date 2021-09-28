package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TargetArgumentsProviderByTypeTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _targetTypeProvider: TargetTypeProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _targetTypeProvider.getTargetType(any()) } answers { if("dll".equals(arg<File>(0).extension, true)) CommandTargetType.Assembly else CommandTargetType.Unknown }
    }

    @DataProvider
    fun cases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(CommandTarget(Path("Abc.dll"))),
                        "True",
                        listOf(TargetArguments(sequenceOf(CommandLineArgument("Abc.dll", CommandLineArgumentType.Target))))
                ),
                arrayOf(
                        sequenceOf(
                                CommandTarget(Path("Abc.dll")),
                                CommandTarget(Path("xyz.dll"))
                        ),
                        "True",
                        listOf(
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("Abc.dll", CommandLineArgumentType.Target),
                                                CommandLineArgument("xyz.dll", CommandLineArgumentType.Target)
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf(
                                CommandTarget(Path("Abc.dll")),
                                CommandTarget(Path("p1.csproj")),
                                CommandTarget(Path("xyz.dll")),
                                CommandTarget(Path("p2.csproj")),
                        ),
                        "True",
                        listOf(
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("Abc.dll", CommandLineArgumentType.Target),
                                                CommandLineArgument("xyz.dll", CommandLineArgumentType.Target)
                                        )
                                ),
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("p1.csproj", CommandLineArgumentType.Target)
                                        )
                                ),
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("p2.csproj", CommandLineArgumentType.Target)
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf(
                                CommandTarget(Path("Abc.dll")),
                                CommandTarget(Path("p1.csproj")),
                                CommandTarget(Path("xyz.dll")),
                                CommandTarget(Path("p2.csproj")),
                        ),
                        null,
                        listOf(
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("Abc.dll", CommandLineArgumentType.Target),
                                        )
                                ),
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("p1.csproj", CommandLineArgumentType.Target)
                                        )
                                ),
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("xyz.dll", CommandLineArgumentType.Target)
                                        )
                                ),
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("p2.csproj", CommandLineArgumentType.Target)
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf(
                                CommandTarget(Path("Abc.dll")),
                                CommandTarget(Path("xyz.dll"))
                        ),
                        "true",
                        listOf(
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("Abc.dll", CommandLineArgumentType.Target),
                                                CommandLineArgument("xyz.dll", CommandLineArgumentType.Target)
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf(
                                CommandTarget(Path("Abc.dll")),
                                CommandTarget(Path("xyz.dll"))
                        ),
                        "false",
                        listOf(
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("Abc.dll", CommandLineArgumentType.Target),
                                        )
                                ),
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("xyz.dll", CommandLineArgumentType.Target)
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf(
                                CommandTarget(Path("Abc.dll")),
                                CommandTarget(Path("xyz.dll"))
                        ),
                        "False",
                        listOf(
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("Abc.dll", CommandLineArgumentType.Target),
                                        )
                                ),
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("xyz.dll", CommandLineArgumentType.Target)
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf(
                                CommandTarget(Path("Abc.dll")),
                                CommandTarget(Path("xyz.dll"))
                        ),
                        "",
                        listOf(
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("Abc.dll", CommandLineArgumentType.Target),
                                        )
                                ),
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("xyz.dll", CommandLineArgumentType.Target)
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf(
                                CommandTarget(Path("Abc.dll")),
                                CommandTarget(Path("xyz.dll"))
                        ),
                        null,
                        listOf(
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("Abc.dll", CommandLineArgumentType.Target),
                                        )
                                ),
                                TargetArguments(
                                        sequenceOf(
                                                CommandLineArgument("xyz.dll", CommandLineArgumentType.Target)
                                        )
                                )
                        )
                )
        )
    }

    @Test(dataProvider = "cases")
    fun shouldProvideTargetType(targets: Sequence<CommandTarget>, singleSessionParamValue: String?, expectedTargetArguments: List<TargetArguments>) {
        // Given
        val provider = createInstance()
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_SINGLE_SESSION) } returns singleSessionParamValue

        // When
        val actualTargetArguments = provider.getTargetArguments(targets).toList()

        // Then
        Assert.assertEquals(actualTargetArguments.size, expectedTargetArguments.size)
        actualTargetArguments.zip(expectedTargetArguments) { a, b -> Assert.assertEquals(a.arguments.toList(), b.arguments.toList())}
    }

    private fun createInstance() = TargetArgumentsProviderByType(_parametersService, _targetTypeProvider)
}