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

using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp.Syntax;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.MsTest;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;
using static Microsoft.CodeAnalysis.CSharp.SyntaxFactory;
using static TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions.RoslynExtensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;

internal class MsTestTestProject : BaseTestProject
{
    public MsTestTestProject() : base(new MsTest()) {}

    protected override IReadOnlyDictionary<string, string> Dependencies { get; } = new Dictionary<string, string>
    {
        { "MSTest.TestAdapter", "2.2.8"},
        { "MSTest.TestFramework", "2.2.8"},
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
                classMembers.Add(PublicMethodWithAttribute(methodName, "TestMethod", Block()));
            }

            var classDeclaration = PublicClassWithAttribute(testClass.ClassName, "TestClass", classMembers.ToArray());
            namespaceMembers.Add(classDeclaration);
        }

        var unitTest = CompilationUnit()
            .WithUsings(
                Using("System"),
                Using("Microsoft.VisualStudio.TestTools.UnitTesting")
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
