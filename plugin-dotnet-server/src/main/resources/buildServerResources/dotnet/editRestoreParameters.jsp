<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<script type="text/javascript">
    BS.DotnetRestoreParametersForm = {
        selectProjectFile: function (chosenFile) {
            var $paths = $j(BS.Util.escapeId('${params.pathsKey}-restore'));
            var value = BS.Util.trimSpaces($paths.val());
            chosenFile = chosenFile.indexOf(" ") >= 0 ? '"' + chosenFile + '"' : chosenFile;
            $paths.val(value.length > 0 ? value + " " + chosenFile : chosenFile);
        }
    };
</script>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.pathsKey}">Projects:</label></th>
    <td>
        <div class="completionIconWrapper clearfix">
            <div class="dnx left">
                <props:textProperty name="${params.pathsKey}" id="${params.pathsKey}-restore" className="longField" expandable="true"/>
            </div>
            <bs:vcsTree callback="BS.DotnetRestoreParametersForm.selectProjectFile"/>
        </div>
        <span class="error" id="error_${params.pathsKey}"></span>
        <span class="smallNote">Space-separated list of project files or folders.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.restoreSourceKey}">NuGet package source:</label></th>
    <td>
        <props:textProperty name="${params.restoreSourceKey}" className="longField"/>
        <span class="error" id="error_${params.restoreSourceKey}"></span>
        <span class="smallNote">Specifies a NuGet package source to use during the restore.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.restorePackagesKey}">Packages path:</label></th>
    <td>
        <props:textProperty name="${params.restorePackagesKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.restorePackagesKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.restorePackagesKey}"></span>
        <span class="smallNote">Directory to install packages in.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"></th>
    <td class="noBorder">
        <props:checkboxProperty name="${params.restoreParallelKey}"/>
        <label for="${params.restoreParallelKey}">Disables restoring multiple project packages in parallel</label>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.verbosityKey}">Logging verbosity:</label></th>
    <td>
        <props:selectProperty name="${params.verbosityKey}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Default&gt;</props:option>
            <c:forEach var="item" items="${params.verbosity}">
                <props:option value="${item}"><c:out value="${item}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.verbosityKey}"></span>
    </td>
</tr>