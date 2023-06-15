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
// public interface ICustomFileSystemWrapper
// {
//     string GetFullPath(string path);
//     
//     bool FileExists(string path);
//
//     (FileInfo?, Exception?) GetFileInfo(string path);
//     
//     (DirectoryInfo?, Exception?) GetDirectoryInfo(string path);
//
//     (IFileSystemInfo?, Exception?) GetFileSystemInfo(string path);
//     
//     bool IsFile(FileSystemInfo fileSystemInfo);
//     
//     FileStream CreateFile(string path);
//     
//     void DeleteFile(string path);
//
//     void MoveFile(string sourcePath, string destinationPath);
//
//     Task CopyFile(string source, string target);
//
//     IAsyncEnumerable<(string, int)> ReadLinesAsync(string path);
//
//     bool DirectoryExists(string path);
//
//     string GetExtension(string path);
//     
//     string ChangeFileExtension(string path, string extension);
//
//     Task AppendAllLinesAsync(string filePath, IEnumerable<string> content);
// }