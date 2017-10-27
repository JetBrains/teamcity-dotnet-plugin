<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.pathName["publish"] = "Projects";
    BS.DotnetParametersForm.pathHint["publish"] = "Specify paths to projects and solutions";
</script>

<tr class="advancedSetting">
    <th><label for="${params.publishFrameworkKey}">Framework:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.publishFrameworkKey}" className="longField"/>
            <bs:projectData type="DotnetFrameworks" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.publishFrameworkKey}" popupTitle="Select frameworks"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.publishFrameworkKey}"></span>
        <span class="smallNote">Target framework to compile for.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.publishConfigKey}">Configuration:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.publishConfigKey}" className="longField"/>
            <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.publishConfigKey}" popupTitle="Select configurations"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.publishConfigKey}"></span>
        <span class="smallNote">Configuration under which to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.publishRuntimeKey}">Runtime:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.publishRuntimeKey}" className="longField"/>
            <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.publishRuntimeKey}" popupTitle="Select runtime"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.publishRuntimeKey}"></span>
        <span class="smallNote">Target runtime to publish for.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.publishOutputKey}">Output directory:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.publishOutputKey}" className="longField"/>
            <bs:vcsTree fieldId="${params.publishOutputKey}" dirsOnly="true"/>
        </div>
        <span class="error" id="error_${params.publishOutputKey}"></span>
        <span class="smallNote">Path where to publish the app.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.publishVersionSuffixKey}">Version suffix:</label></th>
    <td>
        <props:textProperty name="${params.publishVersionSuffixKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.publishVersionSuffixKey}"></span>
        <span class="smallNote">Defines the value for the $(VersionSuffix) property in the project.</span>
    </td>
</tr>