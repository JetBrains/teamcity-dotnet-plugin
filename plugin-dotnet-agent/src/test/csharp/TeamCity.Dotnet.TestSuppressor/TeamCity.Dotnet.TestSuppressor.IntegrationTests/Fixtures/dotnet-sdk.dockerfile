ARG DOTNET_SDK_IMAGE_TAG=6.0
FROM mcr.microsoft.com/dotnet/sdk:${DOTNET_SDK_IMAGE_TAG} AS build

WORKDIR /app

# restore all necessary packages before tests started to make sure that all packages are cached
RUN dotnet new console; \
    dotnet add package Microsoft.NET.Test.Sdk --version 17.3.2 --no-restore; \
    dotnet add package MSTest.TestAdapter --version 2.2.8 --no-restore; \
    dotnet add package MSTest.TestFramework --version 2.2.8 --no-restore; \
    dotnet add package NUnit --version 3.13.3 --no-restore; \
    dotnet add package NUnit3TestAdapter --version 4.2.1 --no-restore; \
    dotnet add package xunit --version 2.4.1 --no-restore; \
    dotnet add package xunit.runner.visualstudio --version 2.4.3 --no-restore; \
    dotnet restore

RUN rm -rf /app     # clean up before copying the actual test project