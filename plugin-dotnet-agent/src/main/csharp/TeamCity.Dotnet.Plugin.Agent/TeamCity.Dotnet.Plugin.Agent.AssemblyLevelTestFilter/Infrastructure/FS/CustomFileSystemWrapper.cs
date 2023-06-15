// /*
//  * Copyright 2000-2023 JetBrains s.r.o.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  * http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */
//
// using System.IO.Abstractions;
//
// namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;
//
// internal class CustomFileSystemWrapper : ICustomFileSystemWrapper
// {
//     private readonly IFileSystem _fileSystem;
//
//     public CustomFileSystemWrapper(IFileSystem fileSystem)
//     {
//         _fileSystem = fileSystem;
//     }
//     
//     public string GetFullPath(string path) => Path.GetFullPath(path);
//
//     public bool FileExists(string path) => File.Exists(path);
//
//     public (FileInfo?, Exception?) GetFileInfo(string path)
//     {
//         if (!FileExists(path))
//         {
//             return (null, new Exception($"File not found: {path}"));
//         }
//         
//         try
//         {
//             return (new FileInfo(path), null);
//         }
//         catch (Exception exception)
//         {
//             return (null, new Exception($"Can't get file info for {path}", exception));
//         }
//     }
//
//     public (DirectoryInfo?, Exception?) GetDirectoryInfo(string path)
//     {
//         if (!DirectoryExists(path))
//         {
//             return (null, new Exception($"Directory not found: {path}"));
//         }
//         
//         try
//         {
//             return (new DirectoryInfo(path), null);
//         }
//         catch (Exception exception)
//         {
//             return (null, new Exception($"Can't get directory info for {path}", exception));
//         }
//     }
//
//     public (IFileSystemInfo?, Exception?) GetFileSystemInfo(string path)
//     {
//         try
//         {
//             var fileInfo = _fileSystem.FileInfo.Wrap(new FileInfo(path));
//             var directoryInfo = _fileSystem.DirectoryInfo.Wrap(new DirectoryInfo(path));
//
//             if (fileInfo.Exists)
//             {
//                 return (fileInfo, null);
//             }
//             
//             if (directoryInfo.Exists)
//             {
//                 return (directoryInfo, null);
//             }
//                 
//             return (null, new Exception($"Path not found: {path}"));
//         } 
//         catch (Exception exception)
//         {
//             return (null, new Exception($"Can't get file system info for the path {path}", exception));
//         }
//     }
//
//     public bool IsFile(IFileSystemInfo fileSystemInfo) => fileSystemInfo is IFileInfo;
//
//     public void DeleteFile(string path) => File.Delete(path);
//
//     public void MoveFile(string sourcePath, string destinationPath) => File.Move(sourcePath, destinationPath);
//     
//     public async Task CopyFile(string source, string target)
//     {
//         await using var sourceStream = File.OpenRead(source);
//         await using var destinationStream = File.Create(target);
//         await sourceStream.CopyToAsync(destinationStream);
//     }
//
//     public async IAsyncEnumerable<(string, int)> ReadLinesAsync(string path)
//     {
//         var lineNumber = 0;
//         using var reader = new StreamReader(path);
//         while (await reader.ReadLineAsync() is {} line)
//         {
//             lineNumber++;
//             yield return (line, lineNumber);
//         }
//     }
//
//     public bool DirectoryExists(string path) => Directory.Exists(path);
//
//     public string GetExtension(string path) => Path.GetExtension(path);
//     
//     public bool FileHasOneOfExtension(string path, IEnumerable<string> extensions) =>
//         extensions.Any(extension => path.EndsWith(extension, StringComparison.OrdinalIgnoreCase));
//
//     public string ChangeFileExtension(string path, string extension) => Path.ChangeExtension(path, extension);
//
//     public Task AppendAllLinesAsync(string filePath, IEnumerable<string> content) => File.AppendAllLinesAsync(filePath, content);
//     
//     public FileStream CreateFile(string path) => File.Create(path);
// }