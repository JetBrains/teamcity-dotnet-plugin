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

using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

internal class TestClassDetector : ITestClassDetector
{
    private readonly ITestEngineRecognizer _testEngineRecognizer;

    public TestClassDetector(ITestEngineRecognizer testEngineRecognizer)
    {
        _testEngineRecognizer = testEngineRecognizer;
    }
    
    public IEnumerable<TestClass> Detect(IDotnetAssembly assembly) => assembly.Types
        .GroupBy(type => _testEngineRecognizer.RecognizeTestEngines(type))
        .Where(typesByTestEngine => typesByTestEngine.Key.Any()) // filter out types that are not test classes
        .SelectMany(testClassesByTestEngine =>
        {
            var testEngine = testClassesByTestEngine.Key!;
            return testClassesByTestEngine.Select(testClassType => new TestClass(testClassType, testEngine));
        });
}