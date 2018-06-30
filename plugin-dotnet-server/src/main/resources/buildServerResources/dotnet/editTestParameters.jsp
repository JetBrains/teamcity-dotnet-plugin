<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
  BS.DotnetParametersForm.pathName["test"] = "Projects";
  BS.DotnetParametersForm.pathHint["test"] = "Specify paths to projects and solutions";
  BS.DotnetParametersForm.coverageEnabled["test"] = true;
  BS.DotnetParametersForm.initFunctions["test"] = function () {
    $j('label[for="${params.testCaseFilterKey}"]').text('Test case filter:').show();
  };
</script>