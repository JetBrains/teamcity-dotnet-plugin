using Microsoft.Build.Definition;
using Microsoft.Build.Evaluation;
using Microsoft.Build.Evaluation.Context;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.MsBuild;

/// <summary>
/// Represents evaluated MSBuild project (e.g. .csproj)
/// </summary>
internal class MsBuildProject : IDisposable
{
    private readonly ProjectCollection _projectCollection;
    private readonly Project _project;
    
    public MsBuildProject(string projectPath)
    {
        // separate project collection to ensure that MSBuild internal static projects collection is not used
        _projectCollection = new ProjectCollection { IsBuildEnabled = false };
        _project = Project.FromFile(projectPath, new ProjectOptions
        {
            ProjectCollection = _projectCollection,
            // instructs not to reuse state between the different project evaluations that use that evaluation context
            EvaluationContext = EvaluationContext.Create(EvaluationContext.SharingPolicy.Isolated),
        });
    }

    public string OutputPath => _project.GetPropertyValue("OutputPath");
    
    public string TargetFileName => _project.GetPropertyValue("TargetFileName");
    
    public IReadOnlyList<string> TargetFrameworks
    {
        get
        {
            var targetFramework = _project.GetPropertyValue("TargetFramework").Trim();
            var targetFrameworks = _project.GetPropertyValue("TargetFrameworks").Trim().Split(';').ToList();
            if (!string.IsNullOrWhiteSpace(targetFramework))
            {
                targetFrameworks.Add(targetFramework);
            }

            return targetFrameworks;
        }
    }
    
    public void Dispose()
    {
        // to be sure, we have no evaluated project cached in MSBuild internals
        _projectCollection.UnloadProject(_project);
        _projectCollection.UnloadAllProjects();
    }
}