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

using Microsoft.Build.Evaluation;
using Microsoft.Build.Locator;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class ProjectTargetResolvingStrategy : BaseTargetResolvingStrategy, ITargetResolvingStrategy
{
    private readonly ILogger<ProjectTargetResolvingStrategy> _logger;
    
    public override TargetType TargetType => TargetType.Project;
    
    public ProjectTargetResolvingStrategy(
        IFileSystem fileSystem,
        ILogger<ProjectTargetResolvingStrategy> logger) : base(fileSystem, logger)
    {
        _logger = logger;
        var instance = MSBuildLocator.RegisterDefaults();
        _logger.LogDebug(
            "Target project resolver uses MSBuild from {InstallationName} {InstallationVersion} located at the path {InstallationPath}",
            instance.Name,
            instance.Version,
            instance.MSBuildPath
        );
    }

    protected override IEnumerable<string> AllowedTargetExtensions => new[] { FileExtension.CSharpProject };

    public override IEnumerable<(FileInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target project: {Target}", target);
        
        var projectFile = TryToGetTargetFile(target);
        if (projectFile == null)
        {
            _logger.LogWarning("Invalid project target: {Target}", target);
            yield break;
        }
        
        var outputAssemblyPath = GetOutputAssemblyPath(projectFile);
        var assemblyFileInfo = new FileInfo(outputAssemblyPath);
        if (!assemblyFileInfo.Exists)
        {
            _logger.LogWarning("Target project output file {TargetProjectOutputFile} does not exist", assemblyFileInfo);
            yield break;
        }
        
        _logger.LogInformation("Resolved assembly by target project: {Assembly}", assemblyFileInfo.FullName);
        yield return (assemblyFileInfo, TargetType.Assembly);
    }

    private static string GetOutputAssemblyPath(FileInfo projectFile)
    {
        var project = new Project(projectFile.FullName);
        
        // currently we support only default output path and default target file name
        // TODO: support 
        var outputPath = project.GetPropertyValue("OutputPath");
        var targetFileName =
            project.GetPropertyValue("TargetFileName")
            ?? projectFile.Name.Replace(FileExtension.CSharpProject, string.Empty);

        return Path
            .Combine(projectFile.Directory!.FullName, outputPath, targetFileName)
            .Replace('\\', Path.DirectorySeparatorChar)
            .Replace('/', Path.DirectorySeparatorChar);
    }
}