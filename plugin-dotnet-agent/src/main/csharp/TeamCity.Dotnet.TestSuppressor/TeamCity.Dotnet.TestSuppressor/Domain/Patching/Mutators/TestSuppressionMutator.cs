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

using TeamCity.Dotnet.TestSuppressor.Domain.Suppression;
using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Patching.Mutators;

internal class TestSuppressionMutator : IAssemblyMutator<TestSuppressionPatchingCriteria>
{
    private readonly ITestSuppressionDecider _testSuppressionDecider;
    private readonly ITestClassDetector _testClassDetector;
    private readonly ITestsSuppressor _testsSuppressor;
    
    public TestSuppressionMutator(
        ITestSuppressionDecider testSuppressionDecider,
        ITestClassDetector testClassDetector,
        ITestsSuppressor testsSuppressor)
    {
        _testSuppressionDecider = testSuppressionDecider;
        _testClassDetector = testClassDetector;
        _testsSuppressor = testsSuppressor;
    }

    public Type PatchingCriteriaType => typeof(TestSuppressionPatchingCriteria);

    public Task<AssemblyMutationResult> MutateAsync(IDotnetAssembly assembly, TestSuppressionPatchingCriteria criteria)
    {
        var (affectedTypes, affectedMethods) = (0, 0);
        
        // for now suppresses only tests classes, but could be easily extended to test methods
        foreach (var (testClass, detectedTestEngines) in _testClassDetector.Detect(assembly))
        {
            var affectedTestsInClass = detectedTestEngines.Sum(testEngine =>
            {
                var (shouldBeSuppressed, testSelector) =
                    _testSuppressionDecider.Decide(testClass.FullName, criteria.InclusionMode, criteria.TestSelectors);
                if (shouldBeSuppressed)
                {
                    var suppressionResult = _testsSuppressor.SuppressTests(testClass, new TestSuppressionParameters(testEngine, testSelector!));
                    return suppressionResult.SuppressedTests;
                }

                return 0;
            });

            if (affectedTestsInClass <= 0)
            {
                continue;
            }
            
            affectedTypes++;
            affectedMethods += affectedTestsInClass;
        }

        return Task.FromResult(new AssemblyMutationResult(affectedTypes, affectedMethods));
    }

    public Task<AssemblyMutationResult> MutateAsync(IDotnetAssembly assembly, IAssemblyPatchingCriteria criteria) =>
        MutateAsync(assembly, (TestSuppressionPatchingCriteria) criteria);
}