using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp.Syntax;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.Engines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;
using static Microsoft.CodeAnalysis.CSharp.SyntaxFactory;
using static TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions.RoslynExtensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;

internal class XUnitTestProject : BaseTestProject
{
    public XUnitTestProject() : base(new XUnit()) {}

    protected override IReadOnlyDictionary<string, string> Dependencies { get; } = new Dictionary<string, string>
    {
        { "xunit", "2.4.1"},
        { "xunit.runner.visualstudio", "2.4.3"},
        { "Microsoft.NET.Test.Sdk", "17.3.2"},
    };

    protected override (string fileName, string content) GenerateSourceFile(string projectName, params TestClassDescription[] testClasses)
    {
        var namespaceMembers = new List<MemberDeclarationSyntax>();

        foreach (var testClass in testClasses)
        {
            var classMembers = new List<MemberDeclarationSyntax>();

            foreach (var methodName in testClass.TestMethodsNames)
            {
                classMembers.Add(PublicMethodWithAttribute(methodName, "Fact", Block()));
            }

            var classDeclaration = PublicClass(testClass.ClassName, classMembers.ToArray());
            namespaceMembers.Add(classDeclaration);
        }

        var unitTest = CompilationUnit()
            .WithUsings(
                Using("System"),
                Using("Xunit")
            )
            .WithMembers(
                SingletonList<MemberDeclarationSyntax>(
                    Namespace(projectName)
                        .WithMembers(List(namespaceMembers))
                )
            );

        return ($"{projectName}.Tests.cs", unitTest.NormalizeWhitespace().ToFullString());
    }
}
