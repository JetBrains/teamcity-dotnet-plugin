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
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.CSharp.Syntax;
using static Microsoft.CodeAnalysis.CSharp.SyntaxFactory;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;

internal static class RoslynExtensions
{
    public static CompilationUnitSyntax WithUsings(this CompilationUnitSyntax compilationUnit, params UsingDirectiveSyntax[] usings) =>
        compilationUnit.WithUsings(List(usings));

    public static UsingDirectiveSyntax Using(string @namespace) => UsingDirective(IdentifierName(@namespace));

    public static NamespaceDeclarationSyntax Namespace(string @namespace) => NamespaceDeclaration(IdentifierName(@namespace));
    
    public static NamespaceDeclarationSyntax Namespace(string @rootNamespace, string @namespace) =>
        NamespaceDeclaration(QualifiedName(IdentifierName(rootNamespace), IdentifierName(@namespace)));

    public static MemberDeclarationSyntax PublicMethodWithAttribute(string name, string attribute, BlockSyntax block) =>
        MethodDeclaration(PredefinedType(Token(SyntaxKind.VoidKeyword)), Identifier(name))
            .WithModifiers(SyntaxTokenList.Create(Token(SyntaxKind.PublicKeyword)))
            .WithAttributeLists(SingletonList(AttributeList(SingletonSeparatedList(Attribute(IdentifierName(attribute))))))
            .WithBody(block);

    public static ClassDeclarationSyntax PublicClassWithAttribute(
        string name, string attribute, params MemberDeclarationSyntax[] members)
    {
        return ClassDeclaration(name)
            .WithModifiers(SyntaxTokenList.Create(Token(SyntaxKind.PublicKeyword)))
            .WithAttributeLists(SingletonList(AttributeList(
                SingletonSeparatedList(Attribute(IdentifierName(attribute))))))
            .WithMembers(List(members));
    }
    
    public static ClassDeclarationSyntax PublicClass(
        string name, params MemberDeclarationSyntax[] members)
    {
        return ClassDeclaration(name)
            .WithModifiers(SyntaxTokenList.Create(Token(SyntaxKind.PublicKeyword)))
            .WithMembers(List(members));
    }
}