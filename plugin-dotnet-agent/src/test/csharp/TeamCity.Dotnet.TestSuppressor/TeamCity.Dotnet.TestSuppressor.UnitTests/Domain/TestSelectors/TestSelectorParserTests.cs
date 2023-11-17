using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.TestSelectors;

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
        result = _parser.TryParseTestQuery(string.Empty, out _);

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
        var result = _parser.TryParseTestQuery("(InvalidTestQuery)", out _);

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

    [Theory]
    [InlineData("classname", "classname")]
    [InlineData("ClassName", "ClassName")]
    [InlineData("class_name", "class_name")]
    [InlineData("CLASS_NAME", "CLASS_NAME")]
    [InlineData("Namespace.ClassName", "Namespace.ClassName")]
    [InlineData("NamespaceA.NamespaceB.ClassName", "NamespaceA.NamespaceB.ClassName")]
    [InlineData("A.B.C.D.E.F.G.ClassName", "A.B.C.D.E.F.G.ClassName")]
    [InlineData("A.B.C.D.E.F.G.ClassName()", "A.B.C.D.E.F.G.ClassName")]
    [InlineData("A.B.C.D.E.F.G.ClassName((()))", "A.B.C.D.E.F.G.ClassName")]
    [InlineData("ClassName(param1,param2)", "ClassName")]
    [InlineData("ClassName(\"param1\",\"param2\")", "ClassName")]
    [InlineData("ClassName(\"\\()&|=!~\",\"\\()&|=!~\")", "ClassName")]
    [InlineData("NamespaceA.NamespaceB.ClassName(param1,param2)", "NamespaceA.NamespaceB.ClassName")]
    [InlineData("NamespaceA.NamespaceB.ClassName(\"param1\",\"param2\")", "NamespaceA.NamespaceB.ClassName")]
    [InlineData("NamespaceA.NamespaceB.ClassName(\"\\()&|=!~\",\"\\()&|=!~\")", "NamespaceA.NamespaceB.ClassName")]
    public void TryParseTestQuery_Should_Return_TestClassSelector_When_No_Parameters_Are_Provided(
        string className,
        string expectedQuery
    )
    {
        // act
        var result = _parser.TryParseTestQuery(className, out var selector);

        // assert
        Assert.True(result);
        Assert.NotNull(selector);
        Assert.Equal(expectedQuery, selector.Query);
    }
}