<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DnuParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}dnx-settings.css'/>");
    BS.DnuParametersForm = {
        selectProjectFile: function (chosenFile) {
            var $paths = $j(BS.Util.escapeId('${params.pathsKey}'));
            var value = BS.Util.trimSpaces($paths.val());
            chosenFile = chosenFile.indexOf(" ") >= 0 ? '"' + chosenFile + '"' : chosenFile;
            $paths.val(value.length > 0 ? value + " " + chosenFile : chosenFile);
        }
    };
</script>

<props:selectSectionProperty name="${params.commandKey}" title="Command:" note="">
    <tr class="advancedSetting">
        <th class="noBorder"><label for="${params.pathsKey}">Projects:</label></th>
        <td>
            <div class="completionIconWrapper clearfix">
                <div class="dnx left">
                    <props:textProperty name="${params.pathsKey}" className="longField" expandable="true"/>
                </div>
                <bs:vcsTree treeId="${params.pathsKey}" callback="BS.DnuParametersForm.selectProjectFile"/>
            </div>
            <span class="error" id="error_${params.pathsKey}"></span>
            <span class="smallNote">Space-separated list of project files or folders.</span>
        </td>
    </tr>

    <c:forEach items="${params.types}" var="type">
        <props:selectSectionPropertyContent value="${type.name}" caption="${type.name}">
            <jsp:include page="${teamcityPluginResourcesPath}/dnu/${type.editPage}"/>
        </props:selectSectionPropertyContent>
    </c:forEach>
</props:selectSectionProperty>

<tr class="advancedSetting">
    <th><label for="${params.argumentsKey}">Command line parameters:</label></th>
    <td>
        <props:textProperty name="${params.argumentsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.argumentsKey}"></span>
        <span class="smallNote">Enter additional command line parameters to dnu.</span>
    </td>
</tr>
