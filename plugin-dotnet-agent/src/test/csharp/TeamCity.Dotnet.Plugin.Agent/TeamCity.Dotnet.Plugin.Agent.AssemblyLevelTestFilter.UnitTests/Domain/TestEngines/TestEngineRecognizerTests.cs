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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.TestEngines;

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

