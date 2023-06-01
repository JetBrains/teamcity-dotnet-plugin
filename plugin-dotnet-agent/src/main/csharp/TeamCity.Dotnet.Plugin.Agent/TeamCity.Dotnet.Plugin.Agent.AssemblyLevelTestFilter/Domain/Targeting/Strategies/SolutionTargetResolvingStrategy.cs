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

using Microsoft.Build.Construction;
using Microsoft.Extensions.Logging;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class SolutionTargetResolvingStrategy : ITargetResolvingStrategy
{
    private readonly ILogger<SolutionTargetResolvingStrategy> _logger;
    
    public TargetType TargetType => TargetType.Solution;

    public SolutionTargetResolvingStrategy(ILogger<SolutionTargetResolvingStrategy> logger)
    {
        _logger = logger;
    }

    public IEnumerable<(FileInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target solution: {Target}", target);
        
        var solutionFile = new FileInfo(target);
        var solution = SolutionFile.Parse(solutionFile.FullName);

        foreach (var project in solution.ProjectsInOrder)
        {
            if (project.ProjectType != SolutionProjectType.KnownToBeMSBuildFormat)
            {
                _logger.LogDebug("Skipping project of unknown type: {Project}", project.AbsolutePath);
                continue;
            }

            var projectFile = new FileInfo(project.AbsolutePath);
            _logger.LogInformation("Resolved project by target solution: {Project}", projectFile.FullName);
            yield return (projectFile, TargetType.Project);
        }
    }
}