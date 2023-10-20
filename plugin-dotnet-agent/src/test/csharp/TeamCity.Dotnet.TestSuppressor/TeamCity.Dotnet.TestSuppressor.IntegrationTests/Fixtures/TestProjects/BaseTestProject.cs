using System.Diagnostics;
using System.Xml.Linq;
using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;
using TeamCity.Dotnet.TestSuppressor.IntegrationTests.Extensions;

namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;

internal abstract class BaseTestProject : ITestProject
{
    protected abstract IReadOnlyDictionary<string, string> Dependencies { get; }

    public ITestEngine TestEngine { get; }
    
    protected BaseTestProject(ITestEngine testEngine)
    {
        TestEngine = testEngine;
    }

    public virtual async Task GenerateAsync(TestProjectSettings settings, params TestClassDescription[] testClasses)
    {
        var (csprojFileName, csprojFileContent) = GenerateCsproj(settings.TargetFrameworks, settings.ProjectName);
        var (csFileName, csFileContent) = GenerateSourceFile(settings.ProjectName, testClasses);
        var filesMap = new Dictionary<string, string>
        {
            { csprojFileName, csprojFileContent },
            { csFileName, csFileContent }
        };

        foreach (var (fileName, content) in filesMap)
        {
            await File.WriteAllTextAsync(settings.DirectoryPath + "/" + fileName, content);
        }
        
        if (settings.WithSolution)
        {
            var csprojPath = settings.DirectoryPath + "/" + csprojFileName;
            CreateSolutionWithProject(settings.ProjectName, settings.DirectoryPath, csprojPath);
        }
    }
    
    private (string fileName, string content) GenerateCsproj(IReadOnlySet<DotnetVersion> targetFrameworks, string projectName)
    {
        // package references
        var dependencies = Dependencies.Select(d =>
            new XElement("PackageReference", new XAttribute("Include", d.Key), new XAttribute("Version", d.Value))
        ).ToList();

        var targetFrameworksElement = targetFrameworks.Count == 1
            ? new XElement("TargetFramework", targetFrameworks.Single().GetMoniker())
            : new XElement("TargetFrameworks", string.Join(';', targetFrameworks.Select(tf => tf.GetMoniker())));

        var project = new XDocument(
            new XElement("Project",
                new XAttribute("Sdk", "Microsoft.NET.Sdk"),
                new XElement("PropertyGroup",
                    targetFrameworksElement,
                    new XElement("RollForward", "LatestMajor")  //  if target framework os less than installed in the container
                ),

                // also add every package to ../dotnet-sdk.dockerfile to cache them in docker image
                new XElement("ItemGroup", dependencies)
            )
        );

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