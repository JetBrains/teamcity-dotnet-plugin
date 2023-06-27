using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.TestEngines;

public class TestEngineRecognizerTests
{
    private readonly Mock<ITestEngine> _testEngineMock;
    private readonly TestEngineRecognizer _recognizer;

    public TestEngineRecognizerTests()
    {
        _testEngineMock = new Mock<ITestEngine>();
        _recognizer = new TestEngineRecognizer(new[] { _testEngineMock.Object });
    }

    [Fact]
    public void RecognizeTestEngines_ShouldReturnEmpty_WhenNoAttributesMatch()
    {
        // arrange
        var typeMock = new Mock<IDotnetType>();
        typeMock.Setup(t => t.CustomAttributes).Returns(new List<IDotnetCustomAttribute>());
        typeMock.Setup(t => t.Methods).Returns(new List<IDotnetMethod>());

        _testEngineMock.Setup(e => e.TestClassAttributes).Returns(new List<string>());
        _testEngineMock.Setup(e => e.TestMethodAttributes).Returns(new List<string>());

        // act
        var result = _recognizer.RecognizeTestEngines(typeMock.Object);

        // assert
        Assert.Empty(result);
    }

    [Fact]
    public void RecognizeTestEngines_ShouldReturnEngine_WhenTypeHasTestClassAttribute()
    {
        // arrange
        var typeMock = new Mock<IDotnetType>();
        var attrMock = new Mock<IDotnetCustomAttribute>();
        attrMock.Setup(a => a.FullName).Returns("TestClassAttribute");

        typeMock.Setup(t => t.CustomAttributes).Returns(new List<IDotnetCustomAttribute> { attrMock.Object });
        typeMock.Setup(t => t.Methods).Returns(new List<IDotnetMethod>());

        _testEngineMock.Setup(e => e.TestClassAttributes).Returns(new List<string> { "TestClassAttribute" });
        _testEngineMock.Setup(e => e.TestMethodAttributes).Returns(new List<string>());

        // act
        var result = _recognizer.RecognizeTestEngines(typeMock.Object);

        // assert
        Assert.Single(result);
        Assert.Equal(_testEngineMock.Object, result[0]);
    }

    [Fact]
    public void RecognizeTestEngines_ShouldReturnEngine_WhenMethodHasTestMethodAttribute()
    {
        // arrange
        var typeMock = new Mock<IDotnetType>();
        var methodMock = new Mock<IDotnetMethod>();
        var attrMock = new Mock<IDotnetCustomAttribute>();
        attrMock.Setup(a => a.FullName).Returns("TestMethodAttribute");

        methodMock.Setup(m => m.CustomAttributes).Returns(new List<IDotnetCustomAttribute> { attrMock.Object });

        typeMock.Setup(t => t.CustomAttributes).Returns(new List<IDotnetCustomAttribute>());
        typeMock.Setup(t => t.Methods).Returns(new List<IDotnetMethod> { methodMock.Object });

        _testEngineMock.Setup(e => e.TestClassAttributes).Returns(new List<string>());
        _testEngineMock.Setup(e => e.TestMethodAttributes).Returns(new List<string> { "TestMethodAttribute" });

        // act
        var result = _recognizer.RecognizeTestEngines(typeMock.Object);

        // assert
        Assert.Single(result);
        Assert.Equal(_testEngineMock.Object, result[0]);
    }
}

