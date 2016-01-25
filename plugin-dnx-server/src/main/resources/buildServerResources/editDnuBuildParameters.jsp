<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<script type="text/javascript">
    BS.DnuBuildParametersForm = {
        selectProjectFile: function (chosenFile) {
            var $paths = $j(BS.Util.escapeId('${params.buildPathsKey}'));
            var value = BS.Util.trimSpaces($paths.val());
            chosenFile = chosenFile.indexOf(" ") >= 0 ? '"' + chosenFile + '"' : chosenFile;
            $paths.val(value.length > 0 ? value + " " + chosenFile : chosenFile);
        }
    };
</script>

<tr class="advancedSetting">
    <th><label for="${params.buildPathsKey}">Projects:</label></th>
    <td>
        <div class="completionIconWrapper clearfix">
            <div class="dnx left">
                <props:textProperty name="${params.buildPathsKey}" className="longField" expandable="true"/>
            </div>
            <bs:vcsTree treeId="${params.buildPathsKey}" callback="BS.DnuBuildParametersForm.selectProjectFile"/>
        </div>
        <span class="error" id="error_${params.buildPathsKey}"></span>
        <span class="smallNote">Space-separated list of project files or folders.</span>
    </td>
</tr>

<tr>
    <th><label for="${params.buildFrameworkKey}">Framework:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.buildFrameworkKey}" className="longField"/>
            <bs:projectData type="DnxFrameworks" sourceFieldId="${params.buildPathsKey}" targetFieldId="${params.buildFrameworkKey}" popupTitle="Select frameworks"/>
        </div>
        <span class="error" id="error_${params.buildFrameworkKey}"></span>
        <span class="smallNote">List of target frameworks to build.</span>
    </td>
</tr>

<tr>
    <th class="noBorder"><label for="${params.buildConfigKey}">Configuration:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.buildConfigKey}" className="longField"/>
            <bs:projectData type="DnxConfigurations" sourceFieldId="${params.buildPathsKey}" targetFieldId="${params.buildConfigKey}" popupTitle="Select configurations"/>
        </div>
        <span class="error" id="error_${params.buildConfigKey}"></span>
        <span class="smallNote">List of configurations to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.buildOutputKey}">Output directory:</label></th>
    <td>
        <props:textProperty name="${params.buildOutputKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.buildOutputKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.buildOutputKey}"></span>
        <span class="smallNote">Output directory.</span>
    </td>
</tr>