<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DnuParametersProvider"/>

<tr class="advancedSetting">
    <th><label for="${params.packFrameworkKey}">Framework:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.packFrameworkKey}" className="longField"/>
            <bs:projectData type="DnxFrameworks" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.packFrameworkKey}" popupTitle="Select frameworks"/>
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
            <bs:projectData type="DnxConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.packConfigKey}" popupTitle="Select configurations"/>
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