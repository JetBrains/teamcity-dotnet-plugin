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

using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class SuppressNothingByExcludingNotExistingClassTests : IClassFixture<DotnetTestContainerFixture>
{
    private readonly DotnetTestContainerFixture _fixture;

    public SuppressNothingByExcludingNotExistingClassTests(DotnetTestContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Output = output;
    }

    [Theory]
    [InlineData(typeof(XUnitTestProject), DotnetVersion.v8_0_Preview)]
    [InlineData(typeof(XUnitTestProject), DotnetVersion.v7_0)]
    [InlineData(typeof(XUnitTestProject), DotnetVersion.v6_0)]
    [InlineData(typeof(NUnitTestProject), DotnetVersion.v8_0_Preview)]
    [InlineData(typeof(NUnitTestProject), DotnetVersion.v7_0)]
    [InlineData(typeof(NUnitTestProject), DotnetVersion.v6_0)]
    [InlineData(typeof(MsTestTestProject), DotnetVersion.v8_0_Preview)]
    [InlineData(typeof(MsTestTestProject), DotnetVersion.v7_0)]
    [InlineData(typeof(MsTestTestProject), DotnetVersion.v6_0)]
    public async Task Run(Type testProjectGeneratorType, DotnetVersion dotnetVersion)
    {
        // arrange
        await _fixture.ReinitContainerWith(dotnetVersion);
        
        const string projectName = "MyTestProject";
        var testClass0 = new TestClassDescription("TestClass0", "Test0", "Test1", "Test2");
        var testClass1 = new TestClassDescription("TestClass1", "Test0", "Test1", "Test2", "Test3");
        var testClass2 = new TestClassDescription("TestClass2", "Test0", "Test1");
        var testClassesToInclude = new[] { testClass0, testClass1 };
        var testNamesToInclude = testClassesToInclude.GetFullTestMethodsNames(projectName);
        var testNamesToExclude = testClass2.GetFullTestMethodsNames(projectName);

        var (testQueriesFilePath, targetAssemblyPath) = await _fixture.CreateTestProject(
            testProjectGeneratorType,
            dotnetVersion,
            projectName,
            testClassesToInclude,
            testClass2
        );

        // act
        var (beforeTestOutput, beforeTestNamesExecuted) = await _fixture.RunTests(targetAssemblyPath);
        await _fixture.RunFilterApp($"suppress -t {targetAssemblyPath} -l {testQueriesFilePath} -v detailed");
        var (afterTestOutput, afterTestNamesExecuted) = await _fixture.RunTests(targetAssemblyPath);

        // assert
        Assert.Equal(7, beforeTestNamesExecuted.Count);
        Assert.Contains($"Passed!  - Failed:     0, Passed:     7, Skipped:     0, Total:     7", beforeTestOutput.Stdout);
        Assert.Equal(7, afterTestNamesExecuted.Count);
        Assert.Contains($"Passed!  - Failed:     0, Passed:     7, Skipped:     0, Total:     7", afterTestOutput.Stdout);
        Assert.True(testNamesToInclude.ContainsSameElements(beforeTestNamesExecuted));
        Assert.True(beforeTestNamesExecuted.ContainsSameElements(afterTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(beforeTestNamesExecuted).Any());
        Assert.False(testNamesToExclude.Intersect(afterTestNamesExecuted).Any());
    }
}