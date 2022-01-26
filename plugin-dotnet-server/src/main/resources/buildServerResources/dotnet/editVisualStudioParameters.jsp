<%--
  ~ Copyright 2000-2022 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

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
