<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<script type="text/javascript">
    BS.DnuRestoreParametersForm = {
        selectProjectFile: function (chosenFile) {
            var $paths = $j(BS.Util.escapeId('${params.restorePathsKey}'));
            var value = BS.Util.trimSpaces($paths.val());
            chosenFile = chosenFile.indexOf(" ") >= 0 ? '"' + chosenFile + '"' : chosenFile;
            $paths.val(value.length > 0 ? value + " " + chosenFile : chosenFile);
        }
    };
</script>

<tr class="advancedSetting">
    <th><label for="${params.restorePathsKey}">Projects:</label></th>
    <td>
        <div class="completionIconWrapper clearfix">
            <div class="dnx left">
                <props:textProperty name="${params.restorePathsKey}" className="longField" expandable="true"/>
            </div>
            <bs:vcsTree treeId="${params.restorePathsKey}" callback="BS.DnuRestoreParametersForm.selectProjectFile"/>
        </div>
        <span class="error" id="error_${params.restorePathsKey}"></span>
        <span class="smallNote">Space-separated list of project files or folders.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.packagePathsKey}">Packages path:</label></th>
    <td>
        <props:textProperty name="${params.packagePathsKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.packagePathsKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.packagePathsKey}"></span>
        <span class="smallNote">Path to restore packages.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"></th>
    <td class="noBorder">
        <props:checkboxProperty name="${params.parallelExecutionKey}"/>
        <label for="${params.parallelExecutionKey}">Parallel execution for multiple discovered projects</label>
    </td>
</tr>