using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching.Mutators;

internal class TestSuppressionMutator : IAssemblyMutator<TestSuppressionPatchingCriteria>
{
    private readonly ITestsSuppressionResolver _testsSuppressionResolver;
    private readonly ITestEngineRecognizer _testEngineRecognizer;
    private readonly ITestsSuppressor _testsSuppressor;
    
    public TestSuppressionMutator(
        ITestsSuppressionResolver testsSuppressionResolver,
        ITestEngineRecognizer testEngineRecognizer,
        ITestsSuppressor testsSuppressor)
    {
        _testsSuppressionResolver = testsSuppressionResolver;
        _testEngineRecognizer = testEngineRecognizer;
        _testsSuppressor = testsSuppressor;
    }
    
    public Task<AssemblyMutationResult> MutateAsync(AssemblyDefinition assembly, TestSuppressionPatchingCriteria criteria)
    {
        var (affectedTypes, affectedMethods) = (0, 0);
        
        foreach (var (testClass, detectedTestEngines) in DetectTests(assembly))
        {
            var suppressionCriterion = _testsSuppressionResolver.ResolveCriteria(testClass.FullName);
            var affectedTestsInClass = detectedTestEngines.Sum(testEngine =>
                _testsSuppressor.SuppressTests(testClass, testEngine, suppressionCriterion));

            if (affectedTestsInClass <= 0)
            {
                continue;
            }
            affectedTypes++;
            affectedMethods += affectedTestsInClass;
        }

        return Task.FromResult(new AssemblyMutationResult(affectedTypes, affectedMethods));
    }
    
    private IEnumerable<(TypeDefinition testClass, IList<ITestEngine> testEngine)> DetectTests(AssemblyDefinition assembly) => assembly.Modules
        .SelectMany(module => module.Types)
        .GroupBy(type => _testEngineRecognizer.RecognizeTestEngines(type))
        .Where(typesByTestEngine => typesByTestEngine.Key.Any()) // filter out types that are not test classes
        .SelectMany(testClassesByTestEngine =>
        {
            var testEngine = testClassesByTestEngine.Key!;
            return testClassesByTestEngine.Select(testClass => (testClass, testEngine));
        });
}