using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp.Syntax;
using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;
using static Microsoft.CodeAnalysis.CSharp.SyntaxFactory;
using static TeamCity.Dotnet.TestSuppressor.IntegrationTests.Extensions.RoslynExtensions;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;

internal class NUnitTestProject : BaseTestProject
{
    public NUnitTestProject() : base(new NUnit()) {}

    protected override IReadOnlyDictionary<string, string> Dependencies { get; } = new Dictionary<string, string>
    {
        { "NUnit", "3.13.3"},
        { "NUnit3TestAdapter", "4.2.1"},
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
                classMembers.Add(PublicMethodWithAttribute(methodName, "Test", Block()));
            }

            var classDeclaration = PublicClassWithAttribute(testClass.ClassName, "TestFixture", classMembers.ToArray());
            namespaceMembers.Add(classDeclaration);
        }

        var unitTest = CompilationUnit()
            .WithUsings(
                Using("System"),
                Using("NUnit.Framework")
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