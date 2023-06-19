using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.Suppression;

public class TestsSuppressorTests
{
    private readonly TestTestEngine _testEngine = new();
    private readonly TestTestSelector _testSelector = new();
    
    [Fact]
    public void SuppressTests_ShouldSuppressTestClass()
    {
        // arrange
        var suppressionParameters = new TestSuppressionParameters(_testEngine, _testSelector);
        var suppressingStrategyMock = new Mock<ITestSuppressingStrategy<TestTestEngine, TestTestSelector>>();
        var testClass = new Mock<IDotnetType>().Object;

        var suppressingStrategies = new[] { suppressingStrategyMock.Object };
        var loggerMock = new Mock<ILogger<TestsSuppressor>>();

        var testSuppressionResult = new TestSuppressionResult(0, 0);
        suppressingStrategyMock
            .Setup(s => s.SuppressTests(testClass, _testSelector))
            .Returns(testSuppressionResult);
        
        var suppressor = new TestsSuppressor(suppressingStrategies, loggerMock.Object);

        // act
        var result = suppressor.SuppressTests(testClass, suppressionParameters);

        // assert
        Assert.Equal(testSuppressionResult, result);
        suppressingStrategyMock.Verify(s => s.SuppressTests(testClass, _testSelector), Times.Once);
    }
    
    [Fact]
    public void SuppressTests_ShouldThrowException_WhenSuppressingStrategyNotFound()
    {
        // arrange
        var suppressionParameters = new TestSuppressionParameters(_testEngine, _testSelector);
        var testClass = new Mock<IDotnetType>().Object;

        var suppressingStrategies = Enumerable.Empty<ITestSuppressingStrategy>();
        var loggerMock = new Mock<ILogger<TestsSuppressor>>();

        var suppressor = new TestsSuppressor(suppressingStrategies, loggerMock.Object);

        // act
        Action act = () => suppressor.SuppressTests(testClass, suppressionParameters);

        // assert
        Assert.Throws<InvalidOperationException>(act);
    }
    
    [Fact]
    public void SuppressTests_ShouldThrowException_WhenStrategyThrowsException()
    {
        // arrange
        var suppressionParameters = new TestSuppressionParameters(_testEngine, _testSelector);
        var suppressingStrategyMock = new Mock<ITestSuppressingStrategy<TestTestEngine, TestTestSelector>>();
        var testClass = new Mock<IDotnetType>().Object;

        var suppressingStrategies = new[] { suppressingStrategyMock.Object };
        var loggerMock = new Mock<ILogger<TestsSuppressor>>();

        suppressingStrategyMock
            .Setup(s => s.SuppressTests(testClass, _testSelector))
            .Throws<Exception>();
        
        var suppressor = new TestsSuppressor(suppressingStrategies, loggerMock.Object);

        // act
        Action act = () => suppressor.SuppressTests(testClass, suppressionParameters);

        // assert
        Assert.Throws<Exception>(act);
    }

    [Fact]
    public void TestsSuppressorConstructor_ShouldThrowException_WhenStrategyDoesNotImplementRequiredInterface()
    {
        // arrange
        var suppressingStrategyMock = new Mock<ITestSuppressingStrategy>(); // does not implement ITestSuppressingStrategy<,>
        var suppressingStrategies = new[] { suppressingStrategyMock.Object };
        var loggerMock = new Mock<ILogger<TestsSuppressor>>();

        // act
        Action act = () => new TestsSuppressor(suppressingStrategies, loggerMock.Object);

        // assert
        Assert.Throws<InvalidOperationException>(act);
    }
    
    internal class TestTestEngine : ITestEngine
    {
        public string Name => "TestTestEngine";
        public IEnumerable<string> AssemblyNames => Array.Empty<string>();
        public IReadOnlyList<string> TestClassAttributes => Array.Empty<string>();
        public IReadOnlyList<string> TestMethodAttributes => Array.Empty<string>();
    }
    
    internal class TestTestSelector : ITestSelector
    {
        public string Query => "";
    }
}
