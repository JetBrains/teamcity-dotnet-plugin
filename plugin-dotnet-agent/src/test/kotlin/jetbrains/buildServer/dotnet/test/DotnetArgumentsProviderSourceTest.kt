package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.DotnetArgumentsProviderSource
import jetbrains.buildServer.dotnet.arguments.DotnetCommandArgumentsProvider
import jetbrains.buildServer.dotnet.arguments.TargetArguments
import jetbrains.buildServer.runners.CommandLineArgument
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetArgumentsProviderSourceTest {
    private var _ctx: Mockery? = null
    private var _MSBuildLoggerArgumentsProvider: ArgumentsProvider? = null
    private var _customArgumentsProvider: ArgumentsProvider? = null
    private var _verbosityArgumentsProvider: ArgumentsProvider? = null
    private var _buildArgumentsProvider: DotnetCommandArgumentsProvider? = null
    private var _cleanArgumentsProvider: DotnetCommandArgumentsProvider? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _MSBuildLoggerArgumentsProvider = _ctx!!.mock<ArgumentsProvider>(ArgumentsProvider::class.java, "MSBuildLoggerArgumentsProvider")
        _customArgumentsProvider = _ctx!!.mock<ArgumentsProvider>(ArgumentsProvider::class.java, "CustomArgumentsProvider")
        _verbosityArgumentsProvider = _ctx!!.mock<ArgumentsProvider>(ArgumentsProvider::class.java, "VerbosityArgumentsProvider")
        _buildArgumentsProvider = _ctx!!.mock<DotnetCommandArgumentsProvider>(DotnetCommandArgumentsProvider::class.java, "Build")
        _cleanArgumentsProvider = _ctx!!.mock<DotnetCommandArgumentsProvider>(DotnetCommandArgumentsProvider::class.java, "Clean")
    }

    @DataProvider
    fun argumentsData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "clean")), listOf("clean", "CleanArg1", "CleanArg2", "VerbosityArg1", "VerbosityArg2", "CustomArg1", "CustomArg2", "MSBuildArg1", "MSBuildArg2"), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "build")), listOf("build", "my.csprog", "BuildArg1", "BuildArg2", "VerbosityArg1", "VerbosityArg2", "CustomArg1", "CustomArg2", "MSBuildArg1", "MSBuildArg2"), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "send")), emptyList<String>() as Any?, Regex("Unknown dotnet command \"send\"")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "   ")), emptyList<String>() as Any?, Regex("Dotnet command name is empty")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "")), emptyList<String>() as Any?, Regex("Dotnet command name is empty")),
                arrayOf(emptyMap<String, String>(), emptyList<String>() as Any?, Regex("Dotnet command name is empty")))
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

                allowing<DotnetCommandArgumentsProvider>(_buildArgumentsProvider).command
                will(returnValue(DotnetCommand.Build))

                allowing<DotnetCommandArgumentsProvider>(_buildArgumentsProvider).arguments
                will(returnValue(sequenceOf(CommandLineArgument("BuildArg1"), CommandLineArgument("BuildArg2"))))

                allowing<DotnetCommandArgumentsProvider>(_buildArgumentsProvider).targetArguments
                will(returnValue(sequenceOf(TargetArguments(sequenceOf(CommandLineArgument("my.csprog"))))))

                allowing<DotnetCommandArgumentsProvider>(_cleanArgumentsProvider).command
                will(returnValue(DotnetCommand.Clean))

                allowing<DotnetCommandArgumentsProvider>(_cleanArgumentsProvider).arguments
                will(returnValue(sequenceOf(CommandLineArgument("CleanArg1"), CommandLineArgument("CleanArg2"))))

                allowing<DotnetCommandArgumentsProvider>(_cleanArgumentsProvider).targetArguments
                will(returnValue(emptySequence<TargetArguments>()))
            }
        })

        val argumentsProviderSource = DotnetArgumentsProviderSource(
                ParametersServiceStub(parameters),
                ArgumentsServiceStub(),
                _MSBuildLoggerArgumentsProvider!!,
                _customArgumentsProvider!!,
                _verbosityArgumentsProvider!!,
                listOf(_buildArgumentsProvider!!, _cleanArgumentsProvider!!))

        // When
        var actualArguments: List<String> = emptyList();
        try {
            actualArguments = argumentsProviderSource.flatMap { it.arguments }.map { it.value }.toList()
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
}