using Microsoft.Build.Evaluation;
using Microsoft.Build.Locator;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies
{
    internal class ProjectTargetResolvingStrategy : ITargetResolvingStrategy
    {
        public TargetType TargetType => TargetType.Project;

        public async IAsyncEnumerable<FileInfo> FindAssembliesAsync(string target)
        {
            MSBuildLocator.RegisterDefaults();
            var projectFile = new FileInfo(target);
            var project = new Project(projectFile.FullName);
            var outputPath = project.GetPropertyValue("OutputPath");
            var outputType = project.GetPropertyValue("OutputType");
            var targetFileName = project.GetPropertyValue("TargetFileName");

            if (!outputType.Equals("Library", StringComparison.OrdinalIgnoreCase) || projectFile.Directory == null)
            {
                yield break;
            }
            
            var assemblyFileInfo = new FileInfo(Path.Combine(projectFile.Directory.FullName, outputPath, targetFileName));
            yield return await Task.FromResult(assemblyFileInfo);
        }
    }
}