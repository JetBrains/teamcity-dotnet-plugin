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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal record AssemblyPatchingResult(bool IsAssemblyPatched, string AssemblyPath, string BackupPath, AssemblyMutationResult MutationResult)
{
    public static AssemblyPatchingResult NotPatched(string assemblyPath) =>
        new AssemblyPatchingResult(false, assemblyPath, string.Empty, AssemblyMutationResult.Empty);
    
    public static AssemblyPatchingResult Patched(string originalAssemblyPath, string patchedAssemblyPath, AssemblyMutationResult mutationResult) =>
        new AssemblyPatchingResult(true, originalAssemblyPath, patchedAssemblyPath, mutationResult);
}