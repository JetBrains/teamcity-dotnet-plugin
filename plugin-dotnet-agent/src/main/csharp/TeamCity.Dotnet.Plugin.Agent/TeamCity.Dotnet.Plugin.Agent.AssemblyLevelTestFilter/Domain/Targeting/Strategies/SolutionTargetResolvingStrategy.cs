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

using System.IO.Abstractions;
using Microsoft.Build.Construction;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class SolutionTargetResolvingStrategy : BaseTargetResolvingStrategy, ITargetResolvingStrategy
{
    private readonly ILogger<SolutionTargetResolvingStrategy> _logger;
    
    public override TargetType TargetType => TargetType.Solution;

    public SolutionTargetResolvingStrategy(
        IFileSystem fileSystem,
        ILogger<SolutionTargetResolvingStrategy> logger) : base(fileSystem, logger)
    {
        _logger = logger;
    }

    protected override IEnumerable<string> AllowedTargetExtensions => new []{ FileExtension.Solution, FileExtension.SolutionFilter };

    public override IEnumerable<(IFileSystemInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target solution: {Target}", target);

        var solutionFile = TryToGetTargetFile(target);
        if (solutionFile == null)
        {
            _logger.LogWarning("Invalid solution target: {Target}", target);
            yield break;
        }

        var (solution, solutionParsingException) = ParseSolution(solutionFile.FullName);
        if (solutionParsingException != null)
        {
            _logger.LogWarning(solutionParsingException,"Target solution {TargetProject} is invalid", solutionFile.FullName);
            yield break;
        }

        foreach (var project in solution!.ProjectsInOrder)
        {
            if (project.ProjectType != SolutionProjectType.KnownToBeMSBuildFormat)
            {
                _logger.LogDebug("Skipping project of unknown type: {Project}", project.AbsolutePath);
                continue;
            }

            var projectFile = FileSystem.FileInfo.New(project.AbsolutePath);
            _logger.LogInformation("Resolved project by target solution: {Project}", projectFile.FullName);
            
            yield return (projectFile, TargetType.Project);
        }
    }
    
    private static (SolutionFile?, Exception?) ParseSolution(string solutionFilePath)
    {
        try
        {
            return (SolutionFile.Parse(solutionFilePath), null);
        } catch (Exception exception)
        {
            return (null, exception);
        }
    }
}