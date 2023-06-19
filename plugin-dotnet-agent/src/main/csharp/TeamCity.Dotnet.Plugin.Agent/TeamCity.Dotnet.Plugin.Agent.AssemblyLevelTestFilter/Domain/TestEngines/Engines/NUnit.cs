namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.Engines;

internal class NUnit : ITestEngine
{
    public string Name => "NUnit";
    
    public IEnumerable<string> AssemblyNames => new[]
    {
        "nunit.framework"
    };

    public IReadOnlyList<string> TestClassAttributes { get; } = new []
    {
        "NUnit.Framework.TestFixtureAttribute",
        "NUnit.Framework.TestFixtureSourceAttribute"
    };

    public IReadOnlyList<string> TestMethodAttributes { get; } = new []
    {
        "NUnit.Framework.TestAttribute",
        "NUnit.Framework.TestCaseAttribute",
        "NUnit.Framework.TestCaseSourceAttribute"
    };
}