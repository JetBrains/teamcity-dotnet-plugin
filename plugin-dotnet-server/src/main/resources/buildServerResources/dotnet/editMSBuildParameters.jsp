<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.paths["msbuild"] = "Projects";
    BS.DotnetParametersForm.coverageEnabled["msbuild"] = true;
</script>

<c:if test="${params.experimentalMode == true}">
    <tr class="advancedSetting">
        <th><label for="${params.msbuildVersionKey}">MSBuild version:</label></th>
        <td>
            <props:selectProperty name="${params.msbuildVersionKey}" enableFilter="true" className="mediumField">
                <props:option value="">&lt;Default&gt;</props:option>
                <c:forEach var="item" items="${params.msbuildVersions}">
                    <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
                </c:forEach>
            </props:selectProperty>
            <span class="error" id="error_${params.msbuildVersionKey}"></span>
        </td>
    </tr>
</c:if>

<tr class="advancedSetting">
    <th><label for="${params.msbuildTargetsKey}">Targets:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.msbuildTargetsKey}" className="longField"/>
            <bs:projectData type="DotnetTargets" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.msbuildTargetsKey}" popupTitle="Select targets"/>
        </div
        <span class="error" id="error_${params.msbuildTargetsKey}"></span>
        <span class="smallNote">Enter list of build targets.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.msbuildConfigKey}">Configuration:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.msbuildConfigKey}" className="longField"/>
            <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.msbuildConfigKey}" popupTitle="Select configuration"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.msbuildConfigKey}"></span>
        <span class="smallNote">Configuration under which to build.</span>
    </td>
</tr>

<c:if test="${not empty propertiesBean.properties[params.msbuildPlatformKey]}">
    <tr class="advancedSetting">
        <th class="noBorder"><label for="${params.msbuildPlatformKey}">Platform:</label></th>
        <td>
            <div class="position-relative">
                <props:textProperty name="${params.msbuildPlatformKey}" className="longField"/>
                <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                                targetFieldId="${params.msbuildPlatformKey}" popupTitle="Select platform"
                                selectionMode="single"/>
            </div>
            <span class="error" id="error_${params.msbuildPlatformKey}"></span>
            <span class="smallNote">Platform under which to build.</span>
        </td>
    </tr>
</c:if>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.msbuildRuntimeKey}">Runtime:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.msbuildRuntimeKey}" className="longField"/>
            <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.msbuildRuntimeKey}" popupTitle="Select runtime"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.msbuildRuntimeKey}"></span>
        <span class="smallNote">Target runtime to build for.</span>
    </td>
</tr>
