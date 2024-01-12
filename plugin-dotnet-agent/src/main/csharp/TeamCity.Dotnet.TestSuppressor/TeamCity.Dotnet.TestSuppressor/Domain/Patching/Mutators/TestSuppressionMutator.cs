

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