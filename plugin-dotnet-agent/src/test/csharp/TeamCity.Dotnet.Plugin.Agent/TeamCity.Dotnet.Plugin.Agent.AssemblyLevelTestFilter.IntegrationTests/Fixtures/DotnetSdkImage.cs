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

using DotNet.Testcontainers.Builders;
using DotNet.Testcontainers.Images;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures;

public class DotnetSdkImage : IImage
{
    private const string DockerfileName = "dotnet-sdk.dockerfile";
    private readonly DotnetVersion _dotnetVersion;
    private readonly IImage _image;
    
    private static string CurrentDirectory => Directory.GetCurrentDirectory();

    public DotnetSdkImage(DotnetVersion dotnetVersion)
    {
        _dotnetVersion = dotnetVersion;
        _image = new DockerImage("localhost/teamcity/dotnet-plugin/agent/dotnet", "sdk", _dotnetVersion.GetDockerTag());
        new ImageFromDockerfileBuilder()
            .WithDockerfileDirectory(CurrentDirectory)
            .WithDockerfile(DockerfileName)
            .WithName(_image)
            .WithBuildArgument("DOTNET_SDK_IMAGE_TAG", _dotnetVersion.GetDockerTag())
            .WithDeleteIfExists(true)
            .WithCleanUp(true)
            .Build()
            .CreateAsync()
            .Wait();
    }

    public string GetHostname() => _image.GetHostname();

    public string Repository => _image.Repository;

    public string Name => _image.Name;

    public string Tag => _image.Tag;

    public string FullName => _image.FullName;
}