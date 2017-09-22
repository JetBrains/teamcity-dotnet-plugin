package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetCommandSet
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.TargetArguments
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetCommandSetTest {
    private var _ctx: Mockery? = null
    private var _buildCommand: DotnetCommand? = null
    private var _cleanCommand: DotnetCommand? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _buildCommand = _ctx!!.mock<DotnetCommand>(DotnetCommand::class.java, "Build")
        _cleanCommand = _ctx!!.mock<DotnetCommand>(DotnetCommand::class.java, "Clean")
    }

    @DataProvider
    fun argumentsData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "clean")), listOf("clean", "CleanArg1", "CleanArg2"), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "build")), listOf("my.csprog", "BuildArg1", "BuildArg2"), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "send")), emptyList<String>() as Any?, null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "   ")), emptyList<String>() as Any?, null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "")), emptyList<String>() as Any?, null),
                arrayOf(emptyMap<String, String>(), emptyList<String>() as Any?, null))
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>,
            exceptionPattern: Regex?) {
        // Given
        _ctx!!.checking(object : Expectations() {
            init {
                allowing<DotnetCommand>(_buildCommand).commandType
                will(returnValue(DotnetCommandType.Build))

                allowing<DotnetCommand>(_buildCommand).toolResolver
                will(returnValue(DotnetToolResolverStub(File("dotnet"), false)))

                allowing<DotnetCommand>(_buildCommand).arguments
                will(returnValue(sequenceOf(CommandLineArgument("BuildArg1"), CommandLineArgument("BuildArg2"))))

                allowing<DotnetCommand>(_buildCommand).targetArguments
                will(returnValue(sequenceOf(TargetArguments(sequenceOf(CommandLineArgument("my.csprog"))))))

                allowing<DotnetCommand>(_cleanCommand).commandType
                will(returnValue(DotnetCommandType.Clean))

                allowing<DotnetCommand>(_cleanCommand).toolResolver
                will(returnValue(DotnetToolResolverStub(File("dotnet"), true)))

                allowing<DotnetCommand>(_cleanCommand).arguments
                will(returnValue(sequenceOf(CommandLineArgument("CleanArg1"), CommandLineArgument("CleanArg2"))))

                allowing<DotnetCommand>(_cleanCommand).targetArguments
                will(returnValue(emptySequence<TargetArguments>()))
            }
        })

        val dotnetCommandSet = DotnetCommandSet(
                ParametersServiceStub(parameters),
                ArgumentsServiceStub(),
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

                allowing<DotnetCommand>(_buildCommand).isSuccessfulExitCode(10)
                will(returnValue(true))

                oneOf<DotnetCommand>(_cleanCommand).commandType
                will(returnValue(DotnetCommandType.Clean))
            }
        })

        val dotnetCommandSet = DotnetCommandSet(
                ParametersServiceStub(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "build"))),
                ArgumentsServiceStub(),
                listOf(_buildCommand!!, _cleanCommand!!))

        // When
        var actualExitCodes = dotnetCommandSet.commands.map { it.isSuccessfulExitCode(10) }.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualExitCodes, listOf(true))
    }
}