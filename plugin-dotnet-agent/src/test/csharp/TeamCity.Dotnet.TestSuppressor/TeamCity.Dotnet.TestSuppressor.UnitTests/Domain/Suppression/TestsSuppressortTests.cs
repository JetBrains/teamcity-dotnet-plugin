using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.Suppression;
using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.Suppression;

public class TestsSuppressorTests
{
    private readonly TestTestEngine _testEngine = new();
    private readonly TestTestSelector _testSelector = new();
    
    [Fact]
    public void SuppressTests_ShouldSuppressTestClass()
    {
        // arrange
        var suppressionParameters = new TestSuppressionParameters(_testEngine, _testSelector);
        var suppressionStrategyMock = new Mock<ITestSuppressionStrategy>();
        suppressionStrategyMock.Setup(m => m.TestEngineType).Returns(_testEngine.GetType());
        suppressionStrategyMock.Setup(m => m.TestSelectorType).Returns(_testSelector.GetType());
        var testClass = new Mock<IDotnetType>().Object;

        var suppressionStrategies = new[] { suppressionStrategyMock.Object };
        var loggerMock = new Mock<ILogger<TestsSuppressor>>();

        var testSuppressionResult = new TestSuppressionResult(0, 0);
        suppressionStrategyMock
            .Setup(s => s.SuppressTests(testClass, _testSelector))
            .Returns(testSuppressionResult);
        
        var suppressor = new TestsSuppressor(suppressionStrategies, loggerMock.Object);

        // act
        var result = suppressor.SuppressTests(testClass, suppressionParameters);

        // assert
        Assert.Equal(testSuppressionResult, result);
        suppressionStrategyMock.Verify(s => s.SuppressTests(testClass, _testSelector), Times.Once);
    }
    
    [Fact]
    public void SuppressTests_ShouldThrowException_WhenSuppressionStrategyNotFound()
    {
        // arrange
        var suppressionParameters = new TestSuppressionParameters(_testEngine, _testSelector);
        var testClass = new Mock<IDotnetType>().Object;

        var suppressionStrategies = Enumerable.Empty<ITestSuppressionStrategy>();
        var loggerMock = new Mock<ILogger<TestsSuppressor>>();

        var suppressor = new TestsSuppressor(suppressionStrategies, loggerMock.Object);

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
        var suppressionStrategyMock = new Mock<ITestSuppressionStrategy>();
        suppressionStrategyMock.Setup(m => m.TestEngineType).Returns(_testEngine.GetType());
        suppressionStrategyMock.Setup(m => m.TestSelectorType).Returns(_testSelector.GetType());
        var testClass = new Mock<IDotnetType>().Object;

        var suppressionStrategies = new[] { suppressionStrategyMock.Object };
        var loggerMock = new Mock<ILogger<TestsSuppressor>>();

        suppressionStrategyMock
            .Setup(s => s.SuppressTests(testClass, _testSelector))
            .Throws<Exception>();
        
        var suppressor = new TestsSuppressor(suppressionStrategies, loggerMock.Object);

        // act
        Action act = () => suppressor.SuppressTests(testClass, suppressionParameters);

        // assert
        Assert.Throws<Exception>(act);
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
