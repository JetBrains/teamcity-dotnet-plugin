namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.MsTest;

internal class MsTest : ITestEngine
{
    public string Name => "MSTest";

    public IList<string> TestClassAttributes { get; } = new List<string>
    {
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestClassAttribute"
    };

    public IList<string> TestMethodAttributes { get; } = new List<string>
    {
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestMethodAttribute",
        "Microsoft.VisualStudio.TestTools.UnitTesting.DataTestMethodAttribute"
    };
}