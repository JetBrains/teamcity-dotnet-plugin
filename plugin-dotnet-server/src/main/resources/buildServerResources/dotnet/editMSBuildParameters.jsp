

<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
  BS.DotnetParametersForm.pathName["msbuild"] = "Projects";
  BS.DotnetParametersForm.pathHint["msbuild"] = "Specify paths to projects and solutions";
  BS.DotnetParametersForm.coverageEnabled["msbuild"] = true;
  BS.DotnetParametersForm.helpUrl["msbuild"] = "https://docs.microsoft.com/en-us/dotnet/core/tools/dotnet-msbuild";
  BS.DotnetParametersForm.supportsParallelTests["msbuild"] = true;
  BS.DotnetParametersForm.initFunctions["msbuild"] = function () {
    var $version = $j(BS.Util.escapeId('${params.msbuildVersionKey}'));

    if ($version[0].selectedIndex === -1) {
      $version[0].selectedIndex = 0;
      $version.change();
      BS.MultilineProperties.updateVisible();
    }
  };
</script>