<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.pathName["run"] = "Projects";
    BS.DotnetParametersForm.pathHint["run"] = "Specify paths to projects and solutions";
</script>

<tr class="advancedSetting">
    <th><label for="${params.publishFrameworkKey}">Framework:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.runFrameworkKey}" className="longField"/>
            <bs:projectData type="DotnetFrameworks" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.runFrameworkKey}" popupTitle="Select frameworks"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.runFrameworkKey}"></span>
        <span class="smallNote">Target framework to compile for.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.runConfigKey}">Configuration:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.runConfigKey}" className="longField"/>
            <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.runConfigKey}" popupTitle="Select configurations"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.runConfigKey}"></span>
        <span class="smallNote">Configuration under which to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.runRuntimeKey}">Runtime:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.runRuntimeKey}" className="longField"/>
            <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.runRuntimeKey}" popupTitle="Select runtime"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.runRuntimeKey}"></span>
        <span class="smallNote">Target runtime to run for.</span>
    </td>
</tr>
