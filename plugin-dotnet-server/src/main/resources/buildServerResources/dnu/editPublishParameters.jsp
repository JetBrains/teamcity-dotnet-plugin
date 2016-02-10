<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DnuParametersProvider"/>

<tr class="advancedSetting">
    <th><label for="${params.publishFrameworkKey}">Framework:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.publishFrameworkKey}" className="longField"/>
            <bs:projectData type="DnxFrameworks" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.publishFrameworkKey}" popupTitle="Select frameworks"/>
        </div>
        <span class="error" id="error_${params.publishFrameworkKey}"></span>
        <span class="smallNote">Name of the frameworks to include.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.publishConfigKey}">Configuration:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.publishConfigKey}" className="longField"/>
            <bs:projectData type="DnxConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.publishConfigKey}" popupTitle="Select configurations"/>
        </div>
        <span class="error" id="error_${params.publishConfigKey}"></span>
        <span class="smallNote">The configuration to use for deployment.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.publishRuntimeKey}">Runtime:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.publishRuntimeKey}" className="longField"/>
            <bs:projectData type="DnxRuntime" sourceFieldId="${params.publishRuntimeKey}"
                            targetFieldId="${params.publishRuntimeKey}" popupTitle="Select runtime"/>
        </div>
        <span class="error" id="error_${params.publishRuntimeKey}"></span>
        <span class="smallNote">Name or full path of the runtime folder to include, or "active" for current runtime.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.publishOutputKey}">Output directory:</label></th>
    <td>
        <props:textProperty name="${params.publishOutputKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.publishOutputKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.publishOutputKey}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"></th>
    <td class="noBorder">
        <props:checkboxProperty name="${params.publishNativeKey}"/>
        <label for="${params.publishNativeKey}">Build and include native images</label>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"></th>
    <td class="noBorder">
        <props:checkboxProperty name="${params.publishCompileSourcesKey}"/>
        <label for="${params.publishCompileSourcesKey}">Compile sources into NuGet packages</label>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"></th>
    <td class="noBorder">
        <props:checkboxProperty name="${params.publishIncludeSymbolsKey}"/>
        <label for="${params.publishIncludeSymbolsKey}">Include symbols</label>
    </td>
</tr>