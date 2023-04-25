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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

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