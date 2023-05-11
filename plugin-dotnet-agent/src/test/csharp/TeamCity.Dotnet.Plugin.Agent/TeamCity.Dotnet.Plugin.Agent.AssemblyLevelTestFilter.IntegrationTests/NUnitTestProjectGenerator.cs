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
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.CSharp.Syntax;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using static Microsoft.CodeAnalysis.CSharp.SyntaxFactory;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.NUnit;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

internal class NUnitTestProjectGenerator : ITestProjectGenerator<NUnit>
{
    public ITestEngine TestEngine { get; } = new NUnit();

    public async Task GenerateAsync(string directoryPath, string projectName)
    {
        var(csprojFileName, csprojFileContent) = GenerateCsproj(projectName);
        var (csFileName, csFileContent) = GenerateNUnitTestsSourceFile(projectName);
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
                        new XAttribute("Include", "NUnit"),
                        new XAttribute("Version", "3.13.3")),
                    new XElement("PackageReference",
                        new XAttribute("Include", "NUnit3TestAdapter"),
                        new XAttribute("Version", "4.2.1")),
                    new XElement("PackageReference",
                        new XAttribute("Include", "Microsoft.NET.Test.Sdk"),
                        new XAttribute("Version", "17.3.2")))));

        return ($"{projectName}.csproj", project.ToString());
    }

    private static (string fileName, string content) GenerateNUnitTestsSourceFile(string projectName)
    {
        var unitTest = CompilationUnit()
            .WithUsings(
                List(new[]
                {
                    UsingDirective(QualifiedName(IdentifierName("NUnit"), IdentifierName("Framework"))),
                    UsingDirective(IdentifierName("System"))
                }))
            .WithMembers(
                SingletonList<MemberDeclarationSyntax>(
                    NamespaceDeclaration(
                            QualifiedName(
                                IdentifierName(projectName),
                                IdentifierName("Tests")))
                        .WithMembers(
                            SingletonList<MemberDeclarationSyntax>(
                                ClassDeclaration("MyTests")
                                    .WithModifiers(SyntaxTokenList.Create(Token(SyntaxKind.PublicKeyword)))
                                    .WithAttributeLists(SingletonList(AttributeList(
                                SingletonSeparatedList(Attribute(IdentifierName("TestFixture"))))))
                                    .WithMembers(
                                        SingletonList<MemberDeclarationSyntax>(
                                            MethodDeclaration(PredefinedType(Token(SyntaxKind.VoidKeyword)), Identifier("MyTest"))
                                                .WithModifiers(SyntaxTokenList.Create(Token(SyntaxKind.PublicKeyword)))
                                                .WithAttributeLists(SingletonList(AttributeList(
                                            SingletonSeparatedList(Attribute(IdentifierName("Test"))))))
                                                .WithBody(Block())))))));

        return ($"{projectName}.Tests.MyTests.cs", unitTest.NormalizeWhitespace().ToFullString());
    }
}