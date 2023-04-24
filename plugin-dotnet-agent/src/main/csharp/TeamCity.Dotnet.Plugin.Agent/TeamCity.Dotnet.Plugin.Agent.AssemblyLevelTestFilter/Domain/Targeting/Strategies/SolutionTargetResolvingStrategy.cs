using Microsoft.Build.Construction;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class SolutionTargetResolvingStrategy : ITargetResolvingStrategy
{
    private readonly IEnumerable<ITargetResolvingStrategy> _strategies;

    public SolutionTargetResolvingStrategy(IEnumerable<ITargetResolvingStrategy> strategies)
    {
        _strategies = strategies;
    }

    public TargetType TargetType => TargetType.Solution;

    public async IAsyncEnumerable<FileInfo> FindAssembliesAsync(string target)
    {
        var solutionFile = new FileInfo(target);
        var solution = SolutionFile.Parse(solutionFile.FullName);
        var projectStrategy = _strategies.First(s => s.TargetType == TargetType.Project);

        foreach (var project in solution.ProjectsInOrder)
        {
            if (project.ProjectType != SolutionProjectType.KnownToBeMSBuildFormat)
            {
                continue;
            }

            var projectFile = new FileInfo(project.AbsolutePath);
            await foreach (var assemblyFile in projectStrategy.FindAssembliesAsync(projectFile.FullName))
            {
                yield return assemblyFile;
            }
        }
    }
}