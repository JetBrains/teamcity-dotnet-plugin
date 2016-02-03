<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DnxParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}dnx-settings.css'/>");
</script>

<tr class="advancedSetting">
    <th><label for="${params.pathsKey}">Project:</label></th>
    <td>
        <div class="completionIconWrapper clearfix">
            <div class="dnx left">
                <props:textProperty name="${params.pathsKey}" className="longField" expandable="true"/>
            </div>
            <bs:vcsTree treeId="${params.pathsKey}" fieldId="${params.pathsKey}"/>
        </div>
        <span class="error" id="error_${params.pathsKey}"></span>
        <span class="smallNote">Path to the project.json file or the application folder.</span>
    </td>
</tr>

<tr>
    <th class="noBorder"><label for="${params.commandKey}">Command:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.commandKey}" className="longField"/>
            <bs:projectData type="DnxCommands" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.commandKey}" popupTitle="Select command"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.commandKey}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.frameworkKey}">Framework:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.frameworkKey}" className="longField"/>
        </div>
        <span class="error" id="error_${params.frameworkKey}"></span>
        <span class="smallNote">The full .net framework version to use when running, i.e. dnx451, dnx452, dnx46, ...</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.configKey}">Configuration:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.configKey}" className="longField"/>
            <bs:projectData type="DnxConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.configKey}" popupTitle="Select configurations"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.configKey}"></span>
        <span class="smallNote">The configuration to run under.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.appbaseKey}">Base directory:</label></th>
    <td>
        <props:textProperty name="${params.appbaseKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.appbaseKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.appbaseKey}"></span>
        <span class="smallNote">Path to the application base directory.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.libsKey}">Libraries directory:</label></th>
    <td>
        <props:textProperty name="${params.libsKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.libsKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.libsKey}"></span>
        <span class="smallNote">Path used for library look-up.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.packagesKey}">Packages directory:</label></th>
    <td>
        <props:textProperty name="${params.packagesKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.packagesKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.packagesKey}"></span>
        <span class="smallNote">Path to the directory containing packages.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.argumentsKey}">Command line parameters:</label></th>
    <td>
        <props:textProperty name="${params.argumentsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.argumentsKey}"></span>
        <span class="smallNote">Enter additional command line parameters to dnx.</span>
    </td>
</tr>
