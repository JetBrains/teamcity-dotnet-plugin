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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;

internal static class AsciiColorExtensions
{
    public static string Default(this string message) => WithColor(message, ConsoleColor.Gray);
    
    public static string Black(this string message) => WithColor(message, ConsoleColor.Black);
    
    public static string Red(this string message) => WithColor(message, ConsoleColor.Red);
    
    public static string Green(this string message) => WithColor(message, ConsoleColor.Green);
    
    public static string Yellow(this string message) => WithColor(message, ConsoleColor.Yellow);
    
    public static string Blue(this string message) => WithColor(message, ConsoleColor.Blue);
    
    public static string Magenta(this string message) => WithColor(message, ConsoleColor.Magenta);
    
    public static string Cyan(this string message) => WithColor(message, ConsoleColor.Cyan);
    
    public static string White(this string message) => WithColor(message, ConsoleColor.White);
    
    private static string WithColor(this string message, ConsoleColor color) =>
        GetAnsiColorCode(color) + message + GetAnsiColorCode(ConsoleColor.Gray);
    
    private static string GetAnsiColorCode(ConsoleColor color)
    {
        return color switch
        {
            ConsoleColor.Black => "\x1b[30m",
            ConsoleColor.Red => "\x1b[31m",
            ConsoleColor.Green => "\x1b[32m",
            ConsoleColor.Yellow => "\x1b[33m",
            ConsoleColor.Blue => "\x1b[34m",
            ConsoleColor.Magenta => "\x1b[35m",
            ConsoleColor.Cyan => "\x1b[36m",
            ConsoleColor.White => "\x1b[37m",
            _ => "\x1b[39m", // Default
        };
    }
}