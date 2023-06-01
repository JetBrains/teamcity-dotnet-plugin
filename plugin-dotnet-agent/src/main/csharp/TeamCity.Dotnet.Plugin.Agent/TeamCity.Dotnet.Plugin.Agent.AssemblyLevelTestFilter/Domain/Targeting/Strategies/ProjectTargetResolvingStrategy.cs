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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class ProjectTargetResolvingStrategy : ITargetResolvingStrategy
{
    private readonly ILogger<ProjectTargetResolvingStrategy> _logger;
    
    public TargetType TargetType => TargetType.Project;
    
    public ProjectTargetResolvingStrategy(ILogger<ProjectTargetResolvingStrategy> logger)
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

    public IEnumerable<(FileInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target project: {Target}", target);
        
        var projectFile = new FileInfo(target);
        var project = new Project(projectFile.FullName);
        var outputPath = project.GetPropertyValue("OutputPath");
        var targetFileName = project.GetPropertyValue("TargetFileName") ?? projectFile.Name.Replace(".csproj", string.Empty);
        
        var fullPath = Path
            .Combine(projectFile.Directory!.FullName, outputPath, targetFileName)
            .Replace('\\', Path.DirectorySeparatorChar)
            .Replace('/', Path.DirectorySeparatorChar);
        var assemblyFileInfo = new FileInfo(fullPath);
        if (!assemblyFileInfo.Exists)
        {
            _logger.LogWarning("Target project output file {TargetProjectOutputFile} does not exist", assemblyFileInfo);
            yield break;
        }
        
        _logger.LogInformation("Resolved assembly by target project: {Assembly}", assemblyFileInfo.FullName);
        yield return (assemblyFileInfo, TargetType.Assembly);
    }
}