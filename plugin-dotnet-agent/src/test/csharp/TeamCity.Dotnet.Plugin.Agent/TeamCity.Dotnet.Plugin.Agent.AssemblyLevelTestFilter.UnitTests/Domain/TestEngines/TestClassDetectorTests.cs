using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.TestEngines;

public class TestClassDetectorTests
{
    private readonly Mock<ITestEngineRecognizer> _testEngineRecognizerMock;
    private readonly TestClassDetector _detector;

    public TestClassDetectorTests()
    {
        _testEngineRecognizerMock = new Mock<ITestEngineRecognizer>();
        _detector = new TestClassDetector(_testEngineRecognizerMock.Object);
    }

    [Fact]
    public void Detect_ShouldReturnEmpty_WhenAssemblyHasNoTypes()
    {
        var assemblyMock = new Mock<IDotnetAssembly>();
        assemblyMock.Setup(a => a.Types).Returns(new List<IDotnetType>());

        var result = _detector.Detect(assemblyMock.Object);

        Assert.Empty(result);
    }

    [Fact]
    public void Detect_ShouldReturnEmpty_WhenNoTestEnginesRecognizeTypes()
    {
        var typeMock = new Mock<IDotnetType>();
        var assemblyMock = new Mock<IDotnetAssembly>();
        assemblyMock.Setup(a => a.Types).Returns(new List<IDotnetType> { typeMock.Object });

        _testEngineRecognizerMock.Setup(r => r.RecognizeTestEngines(It.IsAny<IDotnetType>())).Returns(new List<ITestEngine>());

        var result = _detector.Detect(assemblyMock.Object);

        Assert.Empty(result);
    }

    [Fact]
    public void Detect_ShouldReturnTestClasses_WhenTestEnginesRecognizeTypes()
    {
        var typeMock = new Mock<IDotnetType>();
        var assemblyMock = new Mock<IDotnetAssembly>();
        assemblyMock.Setup(a => a.Types).Returns(new List<IDotnetType> { typeMock.Object });

        var testEngineMock = new Mock<ITestEngine>();
        testEngineMock.Setup(e => e.Name).Returns("TestEngineName");
        testEngineMock.Setup(e => e.AssembliesNames).Returns(new List<string> { "TestAssemblyName" });
        testEngineMock.Setup(e => e.TestClassAttributes).Returns(new List<string> { "TestClassAttribute" });
        testEngineMock.Setup(e => e.TestMethodAttributes).Returns(new List<string> { "TestMethodAttribute" });
        
        _testEngineRecognizerMock.Setup(r => r.RecognizeTestEngines(It.IsAny<IDotnetType>())).Returns(new List<ITestEngine> { testEngineMock.Object });

        var result = _detector.Detect(assemblyMock.Object);

        Assert.Single(result);
        var testClass = result.First();
        Assert.Equal(typeMock.Object, testClass.Type);
        Assert.Contains(testEngineMock.Object, testClass.TestEngines);
        Assert.Equal("TestEngineName", testClass.TestEngines.First().Name);
        Assert.Contains("TestAssemblyName", testClass.TestEngines.First().AssembliesNames);
        Assert.Contains("TestClassAttribute", testClass.TestEngines.First().TestClassAttributes);
        Assert.Contains("TestMethodAttribute", testClass.TestEngines.First().TestMethodAttributes);
    }
}
