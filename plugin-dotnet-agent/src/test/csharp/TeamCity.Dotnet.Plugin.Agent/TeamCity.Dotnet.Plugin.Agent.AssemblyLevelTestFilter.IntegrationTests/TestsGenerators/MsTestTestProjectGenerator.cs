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

using System.Xml.Linq;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp.Syntax;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.MsTest;
using static Microsoft.CodeAnalysis.CSharp.SyntaxFactory;
using static TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions.RoslynExtensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.TestsGenerators;

internal class MsTestTestProjectGenerator : ITestProjectGenerator
{
    public ITestEngine TestEngine { get; } = new MsTest();

    public async Task GenerateAsync(string directoryPath, string projectName, params TestClassDescription[] testClasses)
    {
        var(csprojFileName, csprojFileContent) = GenerateCsproj(projectName);
        var (csFileName, csFileContent) = GenerateMSTestsSourceFile(projectName, testClasses);
        var filesMap = new Dictionary<string, string>
        {
            { csprojFileName, csprojFileContent },
            { csFileName, csFileContent }
        };
        
        foreach (var (fileName, content) in filesMap)
        {
            await File.WriteAllTextAsync(directoryPath + "/" + fileName, content);
        }
    }

    private static (string fileName, string content) GenerateCsproj(string projectName)
    {
        var project = new XDocument(
            new XElement("Project",
                new XAttribute("Sdk", "Microsoft.NET.Sdk"),

                new XElement("PropertyGroup",
                    new XElement("TargetFramework", "net8.0"),
                    new XElement("IsPackable", "false")),

                new XElement("ItemGroup",
                    new XElement("PackageReference",
                        new XAttribute("Include", "MSTest.TestAdapter"),
                        new XAttribute("Version", "2.2.8")),
                    new XElement("PackageReference",
                        new XAttribute("Include", "MSTest.TestFramework"),
                        new XAttribute("Version", "2.2.8")),
                    new XElement("PackageReference",
                        new XAttribute("Include", "Microsoft.NET.Test.Sdk"),
                        new XAttribute("Version", "17.3.2")))));

        return ($"{projectName}.csproj", project.ToString());
    }

    private static (string fileName, string content) GenerateMSTestsSourceFile(string projectName, params TestClassDescription[] testClasses)
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
