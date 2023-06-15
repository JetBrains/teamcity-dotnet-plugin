namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.Engines;

internal class MsTest : ITestEngine
{
    public string Name => "MSTest";
    
    public IEnumerable<string> AssembliesNames => new[]
    {
        "Microsoft.VisualStudio.TestPlatform.TestFramework"
    };

    public IReadOnlyList<string> TestClassAttributes { get; } = new []
    {
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestClassAttribute"
    };

    public IReadOnlyList<string> TestMethodAttributes { get; } = new []
    {
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestMethodAttribute",
        "Microsoft.VisualStudio.TestTools.UnitTesting.DataTestMethodAttribute"
    };
}