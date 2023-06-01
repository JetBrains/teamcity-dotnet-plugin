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

using Microsoft.Extensions.Logging;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class DirectoryTargetResolvingStrategy : ITargetResolvingStrategy
{
    private readonly ILogger<DirectoryTargetResolvingStrategy> _logger;
    
    public TargetType TargetType => TargetType.Directory;

    public DirectoryTargetResolvingStrategy(ILogger<DirectoryTargetResolvingStrategy> logger)
    {
        _logger = logger;
    }

    public IEnumerable<(FileInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target directory: {Target}", target);
        
        var directory = new DirectoryInfo(target);
        var slnFiles = directory.GetFiles(GetSearchPattern(TargetType.Solution), SearchOption.TopDirectoryOnly);
        var csprojFiles = directory.GetFiles(GetSearchPattern(TargetType.Project), SearchOption.TopDirectoryOnly);

        // not sure how to handle this
        // TODO need to test how `dotnet test` handles this:
        // 1. if there are multiple solutions in the directory
        // 2. if there are multiple projects in the directory
        // 3. if there are both solutions and projects in the directory
        // 4. if there are no solutions or projects in the directory
        if (slnFiles.Length != 0)
        {
            foreach (var slnFile in slnFiles)
            {
                _logger.LogInformation("Resolved solution in target directory: {Solution}", slnFile.FullName);
                yield return (slnFile, TargetType.Solution);
            }
        }
        else if (csprojFiles.Length != 0)
        {
            foreach (var csprojFile in csprojFiles)
            {
                _logger.LogInformation("Resolved project in target directory: {Project}", csprojFile.FullName);
                yield return (csprojFile, TargetType.Project);
            }
        }
        else
        {
            foreach (var assemblyFile in directory.GetFiles(GetSearchPattern(TargetType.Assembly), SearchOption.AllDirectories))
            {
                _logger.LogInformation("Resolved assembly in target directory: {Assembly}", assemblyFile.FullName);
                yield return (assemblyFile, TargetType.Assembly);
            }
        }
    }

    private static string GetSearchPattern(TargetType targetType) => $"*{targetType.FileExtension()}";
}