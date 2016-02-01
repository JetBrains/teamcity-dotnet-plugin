<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<tr class="advancedSetting">
    <th><label for="${params.buildFrameworkKey}">Framework:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.buildFrameworkKey}" className="longField"/>
            <bs:projectData type="DnxFrameworks" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.buildFrameworkKey}" popupTitle="Select frameworks"/>
        </div>
        <span class="error" id="error_${params.buildFrameworkKey}"></span>
        <span class="smallNote">List of target frameworks to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.buildConfigKey}">Configuration:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.buildConfigKey}" className="longField"/>
            <bs:projectData type="DnxConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.buildConfigKey}" popupTitle="Select configurations"/>
        </div>
        <span class="error" id="error_${params.buildConfigKey}"></span>
        <span class="smallNote">List of configurations to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.buildOutputKey}">Output directory:</label></th>
    <td>
        <props:textProperty name="${params.buildOutputKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.buildOutputKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.buildOutputKey}"></span>
        <span class="smallNote">Output directory.</span>
    </td>
</tr>