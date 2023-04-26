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

internal class AssemblyTargetResolvingStrategy : ITargetResolvingStrategy
{
    public TargetType TargetType => TargetType.Assembly;
    
    public IEnumerable<(FileInfo, TargetType)> FindAssembliesAsync(string target)
    {
        var fileInfo = new FileInfo(target);
        if (fileInfo.Exists)
        {
            yield return (fileInfo, TargetType.Assembly);
        }
    }
}