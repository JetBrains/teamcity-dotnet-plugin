<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<script type="text/javascript">
    BS.DnuPackParametersForm = {
        selectProjectFile: function (chosenFile) {
            var $paths = $j(BS.Util.escapeId('${params.publishPathsKey}'));
            var value = BS.Util.trimSpaces($paths.val());
            chosenFile = chosenFile.indexOf(" ") >= 0 ? '"' + chosenFile + '"' : chosenFile;
            $paths.val(value.length > 0 ? value + " " + chosenFile : chosenFile);
        }
    };
</script>

<tr class="advancedSetting">
    <th><label for="${params.packPathsKey}">Projects:</label></th>
    <td>
        <div class="completionIconWrapper clearfix">
            <div class="dnx left">
                <props:textProperty name="${params.packPathsKey}" className="longField" expandable="true"/>
            </div>
            <bs:vcsTree treeId="${params.packPathsKey}" callback="BS.DnuPackParametersForm.selectProjectFile"/>
        </div>
        <span class="error" id="error_${params.packPathsKey}"></span>
        <span class="smallNote">Space-separated list of project files or folders.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.packFrameworkKey}">Framework:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.packFrameworkKey}" className="longField"/>
            <bs:projectData type="DnxFrameworks" sourceFieldId="${params.packFrameworkKey}" targetFieldId="${params.packFrameworkKey}" popupTitle="Select frameworks"/>
        </div>
        <span class="error" id="error_${params.packFrameworkKey}"></span>
        <span class="smallNote">List of target frameworks to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.packConfigKey}">Configuration:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.packConfigKey}" className="longField"/>
            <bs:projectData type="DnxConfigurations" sourceFieldId="${params.packConfigKey}" targetFieldId="${params.packConfigKey}" popupTitle="Select configurations"/>
        </div>
        <span class="error" id="error_${params.packConfigKey}"></span>
        <span class="smallNote">List of configurations to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.packOutputKey}">Output directory:</label></th>
    <td>
        <props:textProperty name="${params.packOutputKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.packOutputKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.packOutputKey}"></span>
    </td>
</tr>