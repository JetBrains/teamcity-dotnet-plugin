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

using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal class AssemblyPatcher : IAssemblyPatcher
{
    private readonly IEnumerable<IAssemblyMutator> _mutators;

    public AssemblyPatcher(IEnumerable<IAssemblyMutator> mutators)
    {
        _mutators = mutators;
    }

    public async Task<AssemblyPatchingResult> TryPatchAsync(FileInfo assemblyFile, IAssemblyPatchingCriteria criteria)
    {
        using var assembly = LoadAssembly(assemblyFile.FullName);

        var mutationResult = await SelectMutator(criteria).MutateAsync(assembly, criteria);
        if (mutationResult is { AffectedTypes: 0, AffectedMethods: 0 })
        {
            return AssemblyPatchingResult.NotPatched(assemblyFile.FullName);
        }

        var (originalAssemblyPath, backupAssemblyPath) = await SaveAssemblyAsync(assembly, assemblyFile.FullName);
        return AssemblyPatchingResult.Patched(originalAssemblyPath, backupAssemblyPath, mutationResult);
    }

    private static AssemblyDefinition LoadAssembly(string assemblyPath)
    {
        var assemblyResolver = new DefaultAssemblyResolver();
        assemblyResolver.AddSearchDirectory(AppDomain.CurrentDomain.BaseDirectory);
        return AssemblyDefinition.ReadAssembly(assemblyPath, new ReaderParameters
        {
            AssemblyResolver = assemblyResolver,
            ReadSymbols = true,                     // read debug symbols if available
            ReadWrite = true,                       // allow writing to the assembly
        });
    }

    private IAssemblyMutator<IAssemblyPatchingCriteria> SelectMutator(IAssemblyPatchingCriteria criteria) =>
        (IAssemblyMutator<IAssemblyPatchingCriteria>) _mutators.First(m => m.GetType().GetInterfaces().First().GetGenericArguments()[0] == criteria.GetType());

    private static async Task<(string, string)> SaveAssemblyAsync(AssemblyDefinition assembly, string originalAssemblyPath)
    {
        var backupAssemblyPath = originalAssemblyPath + "_backup";
        var tmpAssemblyPath = originalAssemblyPath + "_tmp";

        // backup the original assembly
        await using (var sourceStream = File.OpenRead(originalAssemblyPath))
        await using (var destinationStream = File.Create(backupAssemblyPath))
        {
            await sourceStream.CopyToAsync(destinationStream);
        }

        // make a tmp copy of the original assembly
        await using (var sourceStream = File.OpenRead(originalAssemblyPath))
        await using (var destinationStream = File.Create(tmpAssemblyPath))
        {
            await sourceStream.CopyToAsync(destinationStream);
        }

        // save the modified assembly on disk in tmp location and preserve debug symbols if available
        await using (var destinationStream = File.Create(tmpAssemblyPath))
        {
            assembly.Write(destinationStream, new WriterParameters { WriteSymbols = true });
        }

        // replace the original assembly with the modified one
        File.Delete(originalAssemblyPath);
        File.Move(tmpAssemblyPath, originalAssemblyPath);

        return (originalAssemblyPath, backupAssemblyPath);
    }
}
