<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<c:set var="asterisk"> <l:star/></c:set>

<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}dotnet-settings.css'/>");

    var commandId = BS.Util.escapeId('${params.commandKey}');

    BS.DotnetParametersForm = {
        appendProjectFile: [],
        coverageEnabled: [],
        hideLogging: [],
        hideWorkingDirectory: [],
        mandatoryPaths: [],
        initFunctions: [],
        selectProjectFile: function (chosenFile) {
            var $paths = $j(BS.Util.escapeId('${params.pathsKey}'));
            var value = BS.Util.trimSpaces($paths.val());

            var commandName = $j(commandId).val();
            var appendFile = BS.DotnetParametersForm.appendProjectFile.indexOf(commandName) >= 0;
            chosenFile = chosenFile.indexOf(" ") >= 0 ? '"' + chosenFile + '"' : chosenFile;
            $paths.val(appendFile && value.length > 0 ? value + " " + chosenFile : chosenFile);
        },
        paths: [],
        updateElements: function () {
            var commandName = $j(commandId).val();

            var pathsName = BS.DotnetParametersForm.paths[commandName];
            var pathsRow = $j(BS.Util.escapeId('${params.pathsKey}-row'));
            if (pathsName) {
                var label = pathsRow.show().find("label");
                label.text(pathsName + ':');
                if (BS.DotnetParametersForm.mandatoryPaths[commandName]) {
                    label.append('${asterisk}');
                }
            } else {
                pathsRow.hide();
            }

            $j(".runnerFormTable span.error").empty();

            var hideLogging = BS.DotnetParametersForm.hideLogging[commandName];
            $j(BS.Util.escapeId('logging')).toggleClass('hidden', !!hideLogging);

            var coverageEnabled = BS.DotnetParametersForm.coverageEnabled[commandName];
            $j(BS.Util.escapeId('dotnet-coverage')).toggleClass('hidden', !coverageEnabled);

            $j(BS.Util.escapeId('dotnet-help')).attr('href', 'https://docs.microsoft.com/en-us/dotnet/core/tools/dotnet-' + commandName);

            var hideWorkingDirectory = BS.DotnetParametersForm.hideWorkingDirectory[commandName];
            $j(BS.Util.escapeId('teamcity.build.workingDir')).closest('tr').toggleClass('hidden', !!hideWorkingDirectory);

            var init = BS.DotnetParametersForm.initFunctions[commandName];
            if (init) init();
        }
    };

    $j(document).on('change', commandId, function () {
        BS.DotnetParametersForm.updateElements();
    });

    $j(document).on('ready', commandId, function () {
        BS.DotnetParametersForm.updateElements();
    });
</script>

<c:set var="commandTitle">Command:<bs:help urlPrefix="https://docs.microsoft.com/en-us/dotnet/core/tools/" file=""/></c:set>
<props:selectSectionProperty name="${params.commandKey}" title="${commandTitle}" note="">
    <tr id="${params.pathsKey}-row">
        <th class="noBorder"><label for="${params.pathsKey}">Projects:</label></th>
        <td>
            <div class="position-relative">
                <props:textProperty name="${params.pathsKey}" className="longField" expandable="true"/>
                <bs:vcsTree treeId="${params.pathsKey}" callback="BS.DotnetParametersForm.selectProjectFile"/>
            </div>
            <span class="error" id="error_${params.pathsKey}"></span>
            <span class="smallNote">Specify target files separated by spaces or new lines. <bs:helpLink file="Wildcards">Wildcards</bs:helpLink> are supported.</span>
        </td>
    </tr>

    <props:workingDirectory/>

    <c:forEach items="${params.commands}" var="type">
        <props:selectSectionPropertyContent value="${type.name}" caption="${type.description}">
            <jsp:include page="${teamcityPluginResourcesPath}/dotnet/${type.editPage}"/>
        </props:selectSectionPropertyContent>
    </c:forEach>
</props:selectSectionProperty>

<tr class="advancedSetting">
    <th><label for="${params.argumentsKey}">Command line parameters:</label></th>
    <td>
        <props:textProperty name="${params.argumentsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.argumentsKey}"></span>
        <span class="smallNote">
            Enter additional command line parameters for dotnet. <a
            id="dotnet-help" target="_blank" showdiscardchangesmessage="false"><bs:helpIcon iconTitle=""/></a>
        </span>
    </td>
</tr>

<tr class="advancedSetting" id="logging">
    <th><label for="${params.verbosityKey}">Logging verbosity:</label></th>
    <td>
        <props:selectProperty name="${params.verbosityKey}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Default&gt;</props:option>
            <c:forEach var="item" items="${params.verbosity}">
                <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.verbosityKey}"></span>
    </td>
</tr>
</tbody>

<tbody id="dotnet-coverage" class="hidden">
    <l:settingsGroup title=".NET Coverage">
        <c:if test="${propertiesBean.properties['dotNetCoverage.dotCover.enabled'] == 'true'}">
            <c:set target="${propertiesBean.properties}" property="${params.coverageTypeKey}" value="dotCover"/>
        </c:if>
        <c:set var="toolsTitle">Code Coverage:<bs:help file="Configuring+.NET+Code+Coverage"/></c:set>
        <props:selectSectionProperty name="${params.coverageTypeKey}" title="${toolsTitle}" note="">
            <props:selectSectionPropertyContent value="" caption="<No .NET Coverage>"/>
            <c:forEach items="${params.coverages}" var="type">
                <props:selectSectionPropertyContent value="${type.name}" caption="${type.description}">
                    <jsp:include page="${teamcityPluginResourcesPath}/coverage/${type.editPage}"/>
                </props:selectSectionPropertyContent>
            </c:forEach>
        </props:selectSectionProperty>
    </l:settingsGroup>
</tbody>

<tbody>

<c:if test="${params.experimentalMode == true}">
    <tr class="advancedSetting">
        <th><label for="${params.integrationPackagePathKey}">Integration package: </label></th>
        <td>
            <jsp:include page="/tools/selector.html?toolType=${params.integrationPackageToolTypeKey}&versionParameterName=${params.integrationPackagePathKey}&class=${clazz}"/>
        </td>
    </tr>
</c:if>

<script type="text/javascript">
  BS.DotnetParametersForm.updateElements();
</script>