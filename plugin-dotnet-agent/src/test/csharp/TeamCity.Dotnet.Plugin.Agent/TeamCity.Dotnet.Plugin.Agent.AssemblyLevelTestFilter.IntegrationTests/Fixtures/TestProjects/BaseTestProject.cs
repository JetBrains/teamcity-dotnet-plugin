using System.Diagnostics;
using System.Xml.Linq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;

internal abstract class BaseTestProject : ITestProject
{
    protected abstract IReadOnlyDictionary<string, string> Dependencies { get; }

    public ITestEngine TestEngine { get; }
    
    protected BaseTestProject(ITestEngine testEngine)
    {
        TestEngine = testEngine;
    }

    public virtual async Task GenerateAsync(DotnetVersion dotnetVersion, string directoryPath, string projectName, bool withSolution,
        params TestClassDescription[] testClasses)
    {
        var (csprojFileName, csprojFileContent) = GenerateCsproj(dotnetVersion, projectName);
        var (csFileName, csFileContent) = GenerateSourceFile(projectName, testClasses);
        var filesMap = new Dictionary<string, string>
        {
            { csprojFileName, csprojFileContent },
            { csFileName, csFileContent }
        };

        foreach (var (fileName, content) in filesMap)
        {
            await File.WriteAllTextAsync(directoryPath + "/" + fileName, content);
        }
        
        if (withSolution)
        {
            var csprojPath = directoryPath + "/" + csprojFileName;
            CreateSolutionWithProject(projectName, directoryPath, csprojPath);
        }
    }
    
    private (string fileName, string content) GenerateCsproj(DotnetVersion dotnetVersion, string projectName)
    {
        // package references
        var dependencies = Dependencies.Select(d =>
            new XElement("PackageReference", new XAttribute("Include", d.Key), new XAttribute("Version", d.Value))
        ).ToList();

        var project = new XDocument(
            new XElement("Project",
                new XAttribute("Sdk", "Microsoft.NET.Sdk"),

                new XElement("PropertyGroup",
                    new XElement("TargetFramework", dotnetVersion.GetMoniker()),
                    new XElement("IsPackable", "false")),

                // also add every package to ../dotnet-sdk.dockerfile to cache them in docker image
                new XElement("ItemGroup", dependencies)
            ));

        return ($"{projectName}.csproj", project.ToString());
    }

    private static void CreateSolutionWithProject(string projectName, string solutionDirPath, string csprojPath)
    {
        // create a new solution
        var process = Process.Start(new ProcessStartInfo
        {
            FileName = "dotnet",
            Arguments = $"new sln -n {projectName} -o {solutionDirPath}",
            RedirectStandardError = true,
        })!;
        process.WaitForExit();
        if (process.ExitCode != 0)
        {
            throw new Exception($"Failed to create solution: {process.StandardError.ReadToEnd()}");
        }
        
        var solutionPath = $"{solutionDirPath}/{projectName}.sln";

        // add the .csproj to the solution
        process = Process.Start(new ProcessStartInfo
        {
            FileName = "dotnet",
            Arguments = $"sln {solutionPath} add {csprojPath}",
            RedirectStandardError = true,
        })!;
        process.WaitForExit();
        if (process.ExitCode != 0)
        {
            throw new Exception($"Failed to add project to solution: {process.StandardError.ReadToEnd()}");
        }
    }


    protected abstract (string fileName, string content) GenerateSourceFile(string projectName, params TestClassDescription[] testClasses);
}