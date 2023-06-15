using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.TestSelectors;

public class TestSelectorParserTests
{
    private readonly Mock<ILogger<TestSelectorParser>> _loggerMock = new();
    private readonly ITestSelectorParser _parser;

    public TestSelectorParserTests()
    {
        _parser = new TestSelectorParser(_loggerMock.Object);
    }

    [Fact]
    public void TryParseTestQuery_Should_Log_Warning_When_Query_Is_Null_Or_Empty()
    {
        // act
        var result = _parser.TryParseTestQuery("", out _);

        // assert
        Assert.False(result);
        _loggerMock.Verify(
            x => x.Log(
                LogLevel.Warning,
                It.IsAny<EventId>(),
                It.Is<It.IsAnyType>((v, t) => v.ToString()!.Contains("Test query couldn't be empty")),
                It.IsAny<Exception>(),
                It.IsAny<Func<It.IsAnyType, Exception, string>>()!));

        // act
        result = _parser.TryParseTestQuery(string.Empty, out var _);

        // assert
        Assert.False(result);
        _loggerMock.Verify(
            x => x.Log(
                LogLevel.Warning,
                It.IsAny<EventId>(),
                It.Is<It.IsAnyType>((v, t) => v.ToString()!.Contains("Test query couldn't be empty")),
                It.IsAny<Exception>(),
                It.IsAny<Func<It.IsAnyType, Exception, string>>()!));
    }

    [Fact]
    public void TryParseTestQuery_Should_Log_Warning_When_Query_Is_Invalid()
    {
        // act
        var result = _parser.TryParseTestQuery("InvalidTestQuery", out var _);

        // assert
        Assert.False(result);
        _loggerMock.Verify(
            x => x.Log(
                LogLevel.Warning,
                It.IsAny<EventId>(),
                It.Is<It.IsAnyType>((v, t) => v.ToString()!.Contains("Invalid test query format")),
                It.IsAny<Exception>(),
                It.IsAny<Func<It.IsAnyType, Exception, string>>()!));
    }

    [Fact]
    public void TryParseTestQuery_Should_Return_TestClassSelector_When_No_Parameters_Are_Provided()
    {
        // act
        var result = _parser.TryParseTestQuery("NamespaceA.NamespaceB.ClassName", out var selector);

        // assert
        Assert.True(result);
        Assert.IsType<TestClassSelector>(selector);
        Assert.Equal("NamespaceA.NamespaceB.ClassName", selector.Query);
    }

    [Fact]
    public void TryParseTestQuery_Should_Return_ParamTestClassSelector_When_Parameters_Are_Provided()
    {
        // act
        var result = _parser.TryParseTestQuery("NamespaceA.NamespaceB.ClassName(param1,param2)", out var selector);

        // assert
        Assert.True(result);
        Assert.IsType<ParamTestClassSelector>(selector);
        Assert.Equal("NamespaceA.NamespaceB.ClassName(param1,param2)", selector.Query);
    }
}


