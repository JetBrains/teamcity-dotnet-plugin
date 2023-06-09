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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching.Mutators;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.Patching.Mutators;

public class TestSuppressionMutatorTests
{
    private readonly Mock<ITestSuppressionDecider> _testSuppressionDeciderMock;
    private readonly Mock<ITestClassDetector> _testClassDetectorMock;
    private readonly Mock<ITestsSuppressor> _testsSuppressorMock;
    private readonly TestSuppressionMutator _mutator;

    public TestSuppressionMutatorTests()
    {
        _testSuppressionDeciderMock = new Mock<ITestSuppressionDecider>();
        _testClassDetectorMock = new Mock<ITestClassDetector>();
        _testsSuppressorMock = new Mock<ITestsSuppressor>();
        _mutator = new TestSuppressionMutator(
            _testSuppressionDeciderMock.Object,
            _testClassDetectorMock.Object,
            _testsSuppressorMock.Object
        );
    }

    [Fact]
    public async Task MutateAsync_TestClassesWereNotDetected_NothingSuppressed()
    {
        // arrange 
        _testClassDetectorMock
            .Setup(m => m.Detect(It.IsAny<IDotnetAssembly>()))
            .Returns(new List<TestClass>());
        var assembly = Mock.Of<IDotnetAssembly>();
        var criteria = new TestSuppressionPatchingCriteria(new Dictionary<string, ITestSelector>(), false);
        
        // act
        var result = await _mutator.MutateAsync(assembly, criteria);
        
        // assert
        Assert.Equal(0, result.AffectedTypes);
        Assert.Equal(0, result.AffectedMethods);
        _testSuppressionDeciderMock
            .Verify(m => 
                m.Decide(It.IsAny<string>(),
                    It.IsAny<bool>(),
                    It.IsAny<IReadOnlyDictionary<string, ITestSelector>>()),
                Times.Never
            );
        _testsSuppressorMock
            .Verify(m =>
                m.SuppressTests(It.IsAny<IDotnetType>(), It.IsAny<TestSuppressionParameters>()),
                Times.Never
            );
    }
    
    [Fact]
    public async Task MutateAsync_NoTestsForSuppressingInAssembly_NothingSuppressed()
    {
        // arrange 
        var testEngines = new List<ITestEngine> { Mock.Of<ITestEngine>() };
        _testClassDetectorMock
            .Setup(m => m.Detect(It.IsAny<IDotnetAssembly>()))
            .Returns(new List<TestClass>
            {
                new (Mock.Of<IDotnetType>(), testEngines),
                new (Mock.Of<IDotnetType>(), testEngines)
            });
        var assembly = Mock.Of<IDotnetAssembly>();
        var criteria = new TestSuppressionPatchingCriteria(new Dictionary<string, ITestSelector>(), false);
        _testSuppressionDeciderMock
            .Setup(m =>
                m.Decide(
                    It.IsAny<string>(),
                    It.IsAny<bool>(),
                    It.IsAny<IReadOnlyDictionary<string, ITestSelector>>()))
            .Returns((false, Mock.Of<ITestSelector>()));
        
        // act
        var result = await _mutator.MutateAsync(assembly, criteria);
        
        // assert
        Assert.Equal(0, result.AffectedTypes);
        Assert.Equal(0, result.AffectedMethods);
        _testSuppressionDeciderMock
            .Verify(m => 
                    m.Decide(It.IsAny<string>(),
                        It.IsAny<bool>(),
                        It.IsAny<IReadOnlyDictionary<string, ITestSelector>>()),
                Times.Exactly(2)
            );
        _testsSuppressorMock
            .Verify(m =>
                    m.SuppressTests(It.IsAny<IDotnetType>(), It.IsAny<TestSuppressionParameters>()),
                Times.Never
            );
    }
    
    [Fact]
    public async Task MutateAsync_SomeTestClassesShouldBeSuppressedInAssembly_Suppressed()
    {
        // arrange 
        var testEngines = new List<ITestEngine> { Mock.Of<ITestEngine>() };
        
        var notSuppressingTestTypeMock = new Mock<IDotnetType>();
        notSuppressingTestTypeMock.Setup(m => m.FullName).Returns("NotSuppressingTestClass");
        var notSuppressingTestType = notSuppressingTestTypeMock.Object;
        
        var suppressingTestTypeMock = new Mock<IDotnetType>();
        suppressingTestTypeMock.Setup(m => m.FullName).Returns("SuppressingTestClass");
        var suppressingTestType = suppressingTestTypeMock.Object;
        var suppressingTestSelector = Mock.Of<ITestSelector>();
        
        _testClassDetectorMock
            .Setup(m => m.Detect(It.IsAny<IDotnetAssembly>()))
            .Returns(new List<TestClass>
            {
                new (notSuppressingTestType, testEngines),
                new (suppressingTestType, testEngines),
                new (notSuppressingTestType, testEngines),
                new (suppressingTestType, testEngines),
            });
        var assembly = Mock.Of<IDotnetAssembly>();
        var criteria = new TestSuppressionPatchingCriteria(new Dictionary<string, ITestSelector>(), false);
        _testSuppressionDeciderMock
            .Setup(m =>
                m.Decide(
                    "NotSuppressingTestClass",
                    It.IsAny<bool>(),
                    It.IsAny<IReadOnlyDictionary<string, ITestSelector>>()))
            .Returns((false, Mock.Of<ITestSelector>()));
        _testSuppressionDeciderMock
            .Setup(m =>
                m.Decide(
                    "SuppressingTestClass",
                    It.IsAny<bool>(),
                    It.IsAny<IReadOnlyDictionary<string, ITestSelector>>()))
            .Returns((true, suppressingTestSelector));
        _testsSuppressorMock
            .Setup(m => m.SuppressTests(suppressingTestType, It.IsAny<TestSuppressionParameters>()))
            .Returns(new TestSuppressionResult(1, 1));
        
        // act
        var result = await _mutator.MutateAsync(assembly, criteria);
        
        // assert
        Assert.Equal(2, result.AffectedTypes);
        Assert.Equal(2, result.AffectedMethods);
        _testSuppressionDeciderMock
            .Verify(m => 
                    m.Decide(It.IsAny<string>(),
                        It.IsAny<bool>(),
                        It.IsAny<IReadOnlyDictionary<string, ITestSelector>>()),
                Times.Exactly(4)
            );
        _testsSuppressorMock
            .Verify(m =>
                    m.SuppressTests(suppressingTestType, It.IsAny<TestSuppressionParameters>()),
                Times.Exactly(2)
            );
        _testsSuppressorMock
            .Verify(m =>
                    m.SuppressTests(notSuppressingTestType, It.IsAny<TestSuppressionParameters>()),
                Times.Never
            );
    }
}
