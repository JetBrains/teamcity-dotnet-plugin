

<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
  BS.DotnetParametersForm.pathName["test"] = "Projects";
  BS.DotnetParametersForm.pathHint["test"] = "Specify paths to projects, solutions, and (since .NET SDK 3.1) test assemblies";
  BS.DotnetParametersForm.excludedPathName["test"] = "Excluded projects";
  BS.DotnetParametersForm.excludedPathHint["test"] = "Specify paths to excluded projects, solutions, and (since .NET SDK 3.1) test assemblies";
  BS.DotnetParametersForm.coverageEnabled["test"] = true;
  BS.DotnetParametersForm.helpUrl["test"] = "https://docs.microsoft.com/en-us/dotnet/core/tools/dotnet-test";
  BS.DotnetParametersForm.supportsParallelTests["test"] = true;
  BS.DotnetParametersForm.initFunctions["test"] = function () {
    $j('label[for="${params.testCaseFilterKey}"]').text('Test case filter:').show();
  };
</script>