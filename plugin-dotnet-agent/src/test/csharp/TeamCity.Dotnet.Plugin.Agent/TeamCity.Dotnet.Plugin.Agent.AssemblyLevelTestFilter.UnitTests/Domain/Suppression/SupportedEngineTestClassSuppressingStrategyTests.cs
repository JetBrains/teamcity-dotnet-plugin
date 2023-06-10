/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression.SuppressingStrategies;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;
using MsTestEngine = TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.Engines.MsTest;
using NUnitEngine = TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.Engines.NUnit;
using XUnitEngine = TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.Engines.XUnit;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.Suppression;

public class SupportedEngineTestClassSuppressingStrategyTests
{
    private readonly Mock<IDotnetType> _typeMock;

    public SupportedEngineTestClassSuppressingStrategyTests()
    {
        _typeMock = new Mock<IDotnetType>();
    }

    [Theory]
    [InlineData(
        typeof(XUnitTestClassSuppressingStrategy),
        typeof(XUnitEngine),
        "ANY",
        "Xunit.FactAttribute",
        1,
        0
    )]
    [InlineData(
        typeof(XUnitTestClassSuppressingStrategy),
        typeof(XUnitEngine),
        "ANY",
        "Xunit.TheoryAttribute",
        1,
        0
    )]
    [InlineData(
        typeof(XUnitTestClassSuppressingStrategy),
        typeof(XUnitEngine),
        "ANY",
        "ANY",
        0,
        0
    )]
    [InlineData(
        typeof(NUnitTestClassSuppressingStrategy),
        typeof(NUnitEngine),
        "NUnit.Framework.TestFixtureAttribute",
        "NUnit.Framework.TestCaseAttribute",
        1,
        1
    )]
    [InlineData(
        typeof(NUnitTestClassSuppressingStrategy),
        typeof(NUnitEngine),
        "NUnit.Framework.TestFixtureSourceAttribute",
        "NUnit.Framework.TestAttribute",
        1,
        1
    )]
    [InlineData(
        typeof(NUnitTestClassSuppressingStrategy),
        typeof(NUnitEngine),
        "ANY",
        "NUnit.Framework.TestCaseSourceAttribute",
        1,
        0
    )]
    [InlineData(
        typeof(NUnitTestClassSuppressingStrategy),
        typeof(NUnitEngine),
        "ANY",
        "ANY",
        0,
        0
    )]
    [InlineData(
        typeof(MsTestTestClassSuppressingStrategy),
        typeof(MsTestEngine),
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestClassAttribute",
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestMethodAttribute",
        1,
        1
    )]
    [InlineData(
        typeof(MsTestTestClassSuppressingStrategy),
        typeof(MsTestEngine),
        "ANY",
        "Microsoft.VisualStudio.TestTools.UnitTesting.DataTestMethodAttribute",
        1,
        0
    )]
    [InlineData(
        typeof(MsTestTestClassSuppressingStrategy),
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

    // [Theory]
    // [InlineData(typeof(XUnitTestClassSuppressingStrategy), typeof (XUnitEngine))]
    // [InlineData(typeof(NUnitTestClassSuppressingStrategy), typeof (NUnitEngine))]
    // [InlineData(typeof(MsTestTestClassSuppressingStrategy), typeof (MsTestEngine))]
    // public void SuppressTestsBySelector_ShouldNotSuppressWhenNoMatchingAttributes(Type strategyType, Type engineType)
    // {
    //     // arrange
    //     var strategy = CreateStrategy(strategyType, engineType);
    //     
    //     var classAttributeMock = new Mock<IDotnetCustomAttribute>();
    //     classAttributeMock.Setup(a => a.FullName).Returns("NonMatchingClassAttribute");
    //
    //     var methodMock = new Mock<IDotnetMethod>();
    //     var methodAttributeMock = new Mock<IDotnetCustomAttribute>();
    //     methodAttributeMock.Setup(a => a.FullName).Returns("NonMatchingMethodAttribute");
    //
    //     methodMock.Setup(m => m.CustomAttributes).Returns(new List<IDotnetCustomAttribute> { methodAttributeMock.Object });
    //     _typeMock.Setup(t => t.CustomAttributes).Returns(new List<IDotnetCustomAttribute> { classAttributeMock.Object });
    //     _typeMock.Setup(t => t.Methods).Returns(new List<IDotnetMethod> { methodMock.Object });
    //
    //     var testClassSelector = new TestClassSelector(new List<string> { "Namespace" }, "ClassName");
    //
    //     // act
    //     var result = strategy.SuppressTests(_typeMock.Object, testClassSelector);
    //
    //     // assert
    //     Assert.Equal(0, result.SuppressedTests);
    //     Assert.Equal(0, result.SuppressedClasses);
    // }

    private static ITestSuppressingStrategy CreateStrategy(Type strategyType, Type engineType)
    {
        var engine = (ITestEngine)Activator.CreateInstance(engineType)!;
        return (ITestSuppressingStrategy)Activator.CreateInstance(strategyType, engine)!;
    }
}

