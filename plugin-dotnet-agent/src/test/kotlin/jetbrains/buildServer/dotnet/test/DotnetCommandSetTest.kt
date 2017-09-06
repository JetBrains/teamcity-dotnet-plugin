package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetCommandSet
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.TargetArguments
import jetbrains.buildServer.runners.CommandLineArgument
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCommandSetTest {
    private var _ctx: Mockery? = null
    private var _MSBuildLoggerArgumentsProvider: ArgumentsProvider? = null
    private var _customArgumentsProvider: ArgumentsProvider? = null
    private var _verbosityArgumentsProvider: ArgumentsProvider? = null
    private var _buildCommand: DotnetCommand? = null
    private var _cleanCommand: DotnetCommand? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _MSBuildLoggerArgumentsProvider = _ctx!!.mock<ArgumentsProvider>(ArgumentsProvider::class.java, "MSBuildLoggerArgumentsProvider")
        _customArgumentsProvider = _ctx!!.mock<ArgumentsProvider>(ArgumentsProvider::class.java, "CustomArgumentsProvider")
        _verbosityArgumentsProvider = _ctx!!.mock<ArgumentsProvider>(ArgumentsProvider::class.java, "VerbosityArgumentsProvider")
        _buildCommand = _ctx!!.mock<DotnetCommand>(DotnetCommand::class.java, "Build")
        _cleanCommand = _ctx!!.mock<DotnetCommand>(DotnetCommand::class.java, "Clean")
    }

    @DataProvider
    fun argumentsData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "clean")), listOf("clean", "CleanArg1", "CleanArg2", "VerbosityArg1", "VerbosityArg2", "CustomArg1", "CustomArg2", "MSBuildArg1", "MSBuildArg2"), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "build")), listOf("build", "my.csprog", "BuildArg1", "BuildArg2", "VerbosityArg1", "VerbosityArg2", "CustomArg1", "CustomArg2", "MSBuildArg1", "MSBuildArg2"), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "send")), emptyList<String>() as Any?, Regex("Unknown dotnet command type \"send\"")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "   ")), emptyList<String>() as Any?, Regex("Dotnet id name is empty")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "")), emptyList<String>() as Any?, Regex("Dotnet id name is empty")),
                arrayOf(emptyMap<String, String>(), emptyList<String>() as Any?, Regex("Dotnet id name is empty")))
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>,
            exceptionPattern: Regex?) {
        // Given
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ArgumentsProvider>(_MSBuildLoggerArgumentsProvider).arguments
                will(returnValue(sequenceOf(CommandLineArgument("MSBuildArg1"), CommandLineArgument("MSBuildArg2"))))

                oneOf<ArgumentsProvider>(_customArgumentsProvider).arguments
                will(returnValue(sequenceOf(CommandLineArgument("CustomArg1"), CommandLineArgument("CustomArg2"))))

                oneOf<ArgumentsProvider>(_verbosityArgumentsProvider).arguments
                will(returnValue(sequenceOf(CommandLineArgument("VerbosityArg1"), CommandLineArgument("VerbosityArg2"))))

                allowing<DotnetCommand>(_buildCommand).commandType
                will(returnValue(DotnetCommandType.Build))

                allowing<DotnetCommand>(_buildCommand).arguments
                will(returnValue(sequenceOf(CommandLineArgument("BuildArg1"), CommandLineArgument("BuildArg2"))))

                allowing<DotnetCommand>(_buildCommand).targetArguments
                will(returnValue(sequenceOf(TargetArguments(sequenceOf(CommandLineArgument("my.csprog"))))))

                allowing<DotnetCommand>(_cleanCommand).commandType
                will(returnValue(DotnetCommandType.Clean))

                allowing<DotnetCommand>(_cleanCommand).arguments
                will(returnValue(sequenceOf(CommandLineArgument("CleanArg1"), CommandLineArgument("CleanArg2"))))

                allowing<DotnetCommand>(_cleanCommand).targetArguments
                will(returnValue(emptySequence<TargetArguments>()))
            }
        })

        val dotnetCommandSet = DotnetCommandSet(
                ParametersServiceStub(parameters),
                ArgumentsServiceStub(),
                _MSBuildLoggerArgumentsProvider!!,
                _customArgumentsProvider!!,
                _verbosityArgumentsProvider!!,
                listOf(_buildCommand!!, _cleanCommand!!))

        // When
        var actualArguments: List<String> = emptyList();
        try {
            actualArguments = dotnetCommandSet.commands.flatMap { it.arguments }.map { it.value }.toList()
            exceptionPattern?.let {
                Assert.fail("Exception should be thrown")
            }
        }
        catch (ex: RunBuildException)
        {
            Assert.assertEquals(exceptionPattern!!.containsMatchIn(ex.message!!), true);
        }

        // Then
        if (exceptionPattern == null) {
            _ctx!!.assertIsSatisfied()
            Assert.assertEquals(actualArguments, expectedArguments)
        }
    }

    @Test
    fun shouldCheckExitCode() {
        // Given
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<DotnetCommand>(_buildCommand).commandType
                will(returnValue(DotnetCommandType.Build))

                allowing<DotnetCommand>(_buildCommand).arguments
                will(returnValue(sequenceOf(CommandLineArgument("BuildArg1"), CommandLineArgument("BuildArg2"))))

                allowing<DotnetCommand>(_buildCommand).targetArguments
                will(returnValue(sequenceOf(TargetArguments(sequenceOf(CommandLineArgument("my.csprog"))))))

                allowing<DotnetCommand>(_buildCommand).isSuccess(10)
                will(returnValue(true))

                oneOf<DotnetCommand>(_cleanCommand).commandType
                will(returnValue(DotnetCommandType.Clean))
            }
        })

        val dotnetCommandSet = DotnetCommandSet(
                ParametersServiceStub(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "build"))),
                ArgumentsServiceStub(),
                _MSBuildLoggerArgumentsProvider!!,
                _customArgumentsProvider!!,
                _verbosityArgumentsProvider!!,
                listOf(_buildCommand!!, _cleanCommand!!))

        // When
        var actualExitCodes = dotnetCommandSet.commands.map { it.isSuccess(10) }.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualExitCodes, listOf(true))
    }
}