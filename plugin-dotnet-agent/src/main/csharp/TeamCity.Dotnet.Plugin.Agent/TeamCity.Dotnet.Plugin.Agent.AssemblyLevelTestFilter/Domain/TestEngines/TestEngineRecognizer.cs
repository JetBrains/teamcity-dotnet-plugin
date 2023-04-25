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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

internal class TestEngineRecognizer : ITestEngineRecognizer
{
    private readonly IEnumerable<ITestEngine> _testEngines;

    public TestEngineRecognizer(IEnumerable<ITestEngine> testEngines)
    {
        _testEngines = testEngines;
    }

    public IList<ITestEngine> RecognizeTestEngines(TypeDefinition type) =>
        _testEngines.Where(engine => HasTestClassAttribute(engine, type) || HasTestMethodAttribute(engine, type)).ToList();

    private static bool HasTestClassAttribute(ITestEngine engine, TypeDefinition type) =>
        type.CustomAttributes.Any(attr => engine.TestClassAttributes.Contains(attr.AttributeType.FullName));

    private static bool HasTestMethodAttribute(ITestEngine engine, TypeDefinition type) =>
        type.Methods.Any(method => method.CustomAttributes.Any(attr => engine.TestMethodAttributes.Contains(attr.AttributeType.FullName)));
}