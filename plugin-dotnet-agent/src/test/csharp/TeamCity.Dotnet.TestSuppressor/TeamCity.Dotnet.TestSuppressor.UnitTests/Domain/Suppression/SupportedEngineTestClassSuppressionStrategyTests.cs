using Moq;
using TeamCity.Dotnet.TestSuppressor.Domain.Suppression;
using TeamCity.Dotnet.TestSuppressor.Domain.Suppression.SuppressionStrategies;
using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;
using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;
using MsTestEngine = TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines.MsTest;
using NUnitEngine = TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines.NUnit;
using XUnitEngine = TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines.XUnit;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Domain.Suppression;

public class SupportedEngineTestClassSuppressionStrategyTests
{
    private readonly Mock<IDotnetType> _typeMock = new();

    [Theory]
    [InlineData(
        typeof(XUnitTestClassSuppressionStrategy),
        typeof(XUnitEngine),
        "ANY",
        "Xunit.FactAttribute",
        1,
        0
    )]
    [InlineData(
        typeof(XUnitTestClassSuppressionStrategy),
        typeof(XUnitEngine),
        "ANY",
        "Xunit.TheoryAttribute",
        1,
        0
    )]
    [InlineData(
        typeof(XUnitTestClassSuppressionStrategy),
        typeof(XUnitEngine),
        "ANY",
        "ANY",
        0,
        0
    )]
    [InlineData(
        typeof(NUnitTestClassSuppressionStrategy),
        typeof(NUnitEngine),
        "NUnit.Framework.TestFixtureAttribute",
        "NUnit.Framework.TestCaseAttribute",
        1,
        1
    )]
    [InlineData(
        typeof(NUnitTestClassSuppressionStrategy),
        typeof(NUnitEngine),
        "NUnit.Framework.TestFixtureSourceAttribute",
        "NUnit.Framework.TestAttribute",
        1,
        1
    )]
    [InlineData(
        typeof(NUnitTestClassSuppressionStrategy),
        typeof(NUnitEngine),
        "ANY",
        "NUnit.Framework.TestCaseSourceAttribute",
        1,
        0
    )]
    [InlineData(
        typeof(NUnitTestClassSuppressionStrategy),
        typeof(NUnitEngine),
        "ANY",
        "ANY",
        0,
        0
    )]
    [InlineData(
        typeof(MsTestTestClassSuppressionStrategy),
        typeof(MsTestEngine),
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestClassAttribute",
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestMethodAttribute",
        1,
        1
    )]
    [InlineData(
        typeof(MsTestTestClassSuppressionStrategy),
        typeof(MsTestEngine),
        "ANY",
        "Microsoft.VisualStudio.TestTools.UnitTesting.DataTestMethodAttribute",
        1,
        0
    )]
    [InlineData(
        typeof(MsTestTestClassSuppressionStrategy),
        typeof(MsTestEngine),
        "ANY",
        "ANY",
        0,
        0
    )]
    public void SuppressTestsBySelector_ShouldSuppressTestClassAndTestMethods(
        Type strategyType,
        Type engineType,
        string classAttribute,
        string methodAttribute,
        int expectedSuppressedTests,
        int expectedSuppressedTestClasses)
    {
        // arrange
        var strategy = CreateStrategy(strategyType, engineType);

        var classAttributeMock = new Mock<IDotnetCustomAttribute>();
        classAttributeMock.Setup(a => a.FullName).Returns(classAttribute);

        var methodMock = new Mock<IDotnetMethod>();
        var methodAttributeMock = new Mock<IDotnetCustomAttribute>();
        methodAttributeMock.Setup(a => a.FullName).Returns(methodAttribute);

        methodMock.Setup(m => m.CustomAttributes).Returns(new List<IDotnetCustomAttribute> { methodAttributeMock.Object });
        _typeMock.Setup(t => t.CustomAttributes).Returns(new List<IDotnetCustomAttribute> { classAttributeMock.Object });
        _typeMock.Setup(t => t.Methods).Returns(new List<IDotnetMethod> { methodMock.Object });

        var testClassSelector = new TestClassSelector(new List<string> { "Namespace" }, "ClassName");

        // act
        var result = strategy.SuppressTests(_typeMock.Object, testClassSelector);

        // assert
        Assert.Equal(expectedSuppressedTests, result.SuppressedTests);
        Assert.Equal(expectedSuppressedTestClasses, result.SuppressedClasses);
    }
    
    private static ITestSuppressionStrategy CreateStrategy(Type strategyType, Type engineType)
    {
        var engine = (ITestEngine)Activator.CreateInstance(engineType)!;
        return (ITestSuppressionStrategy)Activator.CreateInstance(strategyType, engine)!;
    }
}

