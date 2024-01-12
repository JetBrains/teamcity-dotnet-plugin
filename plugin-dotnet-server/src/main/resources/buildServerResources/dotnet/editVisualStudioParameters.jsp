

<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
  BS.DotnetParametersForm.pathName["devenv"] = "Projects";
  BS.DotnetParametersForm.pathHint["devenv"] = "Specify paths to projects and solutions";
  BS.DotnetParametersForm.coverageEnabled["devenv"] = true;
  BS.DotnetParametersForm.hideLogging["devenv"] = true;
  BS.DotnetParametersForm.mandatoryPaths["devenv"] = true;
  BS.DotnetParametersForm.helpUrl["devenv"] = "https://docs.microsoft.com/en-us/visualstudio/ide/reference/devenv-command-line-switches";
  BS.DotnetParametersForm.initFunctions["devenv"] = function () {
    var $version = $j(BS.Util.escapeId('${params.visualStudioVersionKey}'));

    if ($version[0].selectedIndex === -1) {
      $version[0].selectedIndex = 0;
      $version.change();
      BS.MultilineProperties.updateVisible();
    }
  };
</script>