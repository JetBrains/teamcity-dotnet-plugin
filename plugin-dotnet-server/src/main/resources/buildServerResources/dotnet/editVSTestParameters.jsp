<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:set var="asterisk"><l:star/></c:set>

<script type="text/javascript">
  BS.DotnetParametersForm.pathName["vstest"] = "Test assemblies";
  BS.DotnetParametersForm.pathHint["vstest"] = "Specify paths to test assemblies";
  BS.DotnetParametersForm.projectArtifactsSelector["vstest"] = true;
  BS.DotnetParametersForm.mandatoryPaths["vstest"] = true;
  BS.DotnetParametersForm.coverageEnabled["vstest"] = true;
  BS.DotnetParametersForm.initFunctions["vstest"] = function () {
    var optionId = BS.Util.escapeId('${params.testFilterKey}');
    var $testNames = $j('label[for="${params.testNamesKey}"]').closest('tr');
    var $testCaseFilter = $j('label[for="${params.testCaseFilterKey}"]').html('Test case filter: ${asterisk}').closest('tr');

    function updateElements() {
      var filter = $j(optionId).val();
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
