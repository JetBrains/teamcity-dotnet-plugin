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