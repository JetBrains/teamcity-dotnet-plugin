<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<c:set var="asterisk"><l:star/></c:set>

<script type="text/javascript">
  BS.DotnetParametersForm.pathName["vstest"] = "Test assemblies";
  BS.DotnetParametersForm.pathHint["vstest"] = "Specify paths to test assemblies";
  BS.DotnetParametersForm.projectArtifactsSelector["vstest"] = true;
  BS.DotnetParametersForm.mandatoryPaths["vstest"] = true;
  BS.DotnetParametersForm.coverageEnabled["vstest"] = true;
  BS.DotnetParametersForm.helpUrl["vstest"] = "https://docs.microsoft.com/en-us/dotnet/core/tools/dotnet-vstest";
  BS.DotnetParametersForm.initFunctions["vstest"] = function () {
    var optionId = BS.Util.escapeId('${params.testFilterKey}');
    var $testNames = $j('label[for="${params.testNamesKey}"]').closest('tr');
    var $testCaseFilter = $j('label[for="${params.testCaseFilterKey}"]').html('Test case filter: ${asterisk}').closest('tr');

    var $version = $j(BS.Util.escapeId('${params.vstestVersionKey}'));
    if ($version[0].selectedIndex === -1) {
      $version[0].selectedIndex = 0;
      $version.change();
    }

    var $paltofrm = $j(BS.Util.escapeId('${params.platformKey}'));
    if ($paltofrm[0].selectedIndex === -1) {
      $paltofrm[0].selectedIndex = 0;
      $paltofrm.change();
    }

    function updateElements() {
      var filter = $j(optionId).val();
      if (filter !== 'name') {
          BS.DotnetParametersForm.clearInputValues($testNames);
      }
      if (filter !== 'filter') {
          BS.DotnetParametersForm.clearInputValues($testCaseFilter);
      }
      $testNames.toggle(filter === 'name');
      $testCaseFilter.toggle(filter === 'filter');
      BS.MultilineProperties.updateVisible();
    }

    $j(document).on('change', $j(optionId), function () {
      updateElements();
    });

    updateElements();
  };
</script>
