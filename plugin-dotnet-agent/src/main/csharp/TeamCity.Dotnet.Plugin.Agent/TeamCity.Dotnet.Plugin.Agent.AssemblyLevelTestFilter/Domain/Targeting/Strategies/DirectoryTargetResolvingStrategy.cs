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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class DirectoryTargetResolvingStrategy : ITargetResolvingStrategy
{
    private readonly IEnumerable<ITargetResolvingStrategy> _strategies;

    public DirectoryTargetResolvingStrategy(IEnumerable<ITargetResolvingStrategy> strategies)
    {
        _strategies = strategies;
    }

    public TargetType TargetType => TargetType.Directory;

    public async IAsyncEnumerable<FileInfo> FindAssembliesAsync(string target)
    {
        var directory = new DirectoryInfo(target);
        var slnFiles = directory.GetFiles(GetSearchPattern(TargetType.Solution), SearchOption.TopDirectoryOnly);
        var csprojFiles = directory.GetFiles(GetSearchPattern(TargetType.Project), SearchOption.TopDirectoryOnly);

        // not sure how to handle this
        // TODO need to test how `dotnet test` handles this:
        // 1. if there are multiple solutions in the directory
        // 2. if there are multiple projects in the directory
        // 3. if there are both solutions and projects in the directory
        // 4. if there are no solutions or projects in the directory
        if (slnFiles.Any())
        {
            var strategy = _strategies.First(s => s.TargetType == TargetType.Solution);
            foreach (var slnFile in slnFiles)
            {
                await foreach (var assemblyFile in strategy.FindAssembliesAsync(slnFile.FullName))
                {
                    yield return assemblyFile;
                }
            }
        }
        else if (csprojFiles.Any())
        {
            var strategy = _strategies.First(s => s.TargetType == TargetType.Project);
            foreach (var csprojFile in csprojFiles)
            {
                await foreach (var assemblyFile in strategy.FindAssembliesAsync(csprojFile.FullName))
                {
                    yield return assemblyFile;
                }
            }
        }
        else
        {
            foreach (var dllFile in directory.GetFiles(GetSearchPattern(TargetType.Assembly), SearchOption.AllDirectories))
            {
                yield return dllFile;
            }
        }
    }

    private static string GetSearchPattern(TargetType targetType) => $"*{targetType.FileExtension()}";
}