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

using System.IO;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

internal class FileSystemWrapper : IFileSystem
{
    public string GetFullPath(string path) => Path.GetFullPath(path);

    public bool FileExists(string path) => File.Exists(path);

    public void FileDelete(string path) => File.Delete(path);

    public void FileMove(string sourcePath, string destinationPath) => File.Move(sourcePath, destinationPath);

    public async IAsyncEnumerable<(string, int)> ReadLinesAsync(string path)
    {
        var lineNumber = 0;
        using var reader = new StreamReader(path);
        while (await reader.ReadLineAsync() is {} line)
        {
            lineNumber++;
            yield return (line, lineNumber);
        }
    }

    public bool DirectoryExists(string path) => Directory.Exists(path);

    public string GetExtension(string path) => Path.GetExtension(path);
    
    public Task AppendAllLinesAsync(string filePath, IEnumerable<string> content) => File.AppendAllLinesAsync(filePath, content);
}