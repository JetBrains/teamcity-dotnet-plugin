<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.appendProjectFile.push("clean");
    BS.DotnetParametersForm.pathName["clean"] = "Projects";
    BS.DotnetParametersForm.pathHint["clean"] = "Specify paths to projects and solutions";
</script>

<tr class="advancedSetting">
    <th><label for="${params.cleanFrameworkKey}">Framework:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.cleanFrameworkKey}" className="longField"/>
            <bs:projectData type="DotnetFrameworks" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.cleanFrameworkKey}" popupTitle="Select frameworks"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.cleanFrameworkKey}"></span>
        <span class="smallNote">Framework under which to clean.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.cleanConfigKey}">Configuration:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.cleanConfigKey}" className="longField"/>
            <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.cleanConfigKey}" popupTitle="Select configuration"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.cleanConfigKey}"></span>
        <span class="smallNote">Configuration under which to clean.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.cleanRuntimeKey}">Runtime:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.cleanRuntimeKey}" className="longField"/>
            <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.cleanRuntimeKey}" popupTitle="Select runtime"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.cleanRuntimeKey}"></span>
        <span class="smallNote">Target runtime to clean for.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.cleanOutputKey}">Output directory:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.cleanOutputKey}" className="longField"/>
            <bs:vcsTree fieldId="${params.cleanOutputKey}" dirsOnly="true"/>
        </div>
        <span class="error" id="error_${params.cleanOutputKey}"></span>
        <span class="smallNote">Directory where the build outputs are placed.</span>
    </td>
</tr>