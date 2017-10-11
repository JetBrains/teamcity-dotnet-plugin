<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.paths["devenv"] = "Solutions or Projects";
    BS.DotnetParametersForm.coverageEnabled["devenv"] = true;
    BS.DotnetParametersForm.hideLogging["devenv"] = true;
    BS.DotnetParametersForm.targetsAreRequired["devenv"] = true;
</script>

<tr>
    <th><label for="${params.visualStudioVersionKey}">Build Action: <l:star/></label></th>
    <td>
        <props:selectProperty name="${params.visualStudioActionKey}" enableFilter="true" className="mediumField">
            <c:forEach var="item" items="${params.visualStudioActions}">
                <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.visualStudioActionKey}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.visualStudioVersionKey}">Visual Studio Version:</label></th>
    <td>
        <props:selectProperty name="${params.visualStudioVersionKey}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Default&gt;</props:option>
            <c:forEach var="item" items="${params.visualStudioVersions}">
                <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.visualStudioVersionKey}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.visualStudioConfigKey}">Configuration:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.visualStudioConfigKey}" className="longField"/>
            <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.visualStudioConfigKey}" popupTitle="Select configuration"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.visualStudioConfigKey}"></span>
        <span class="smallNote">Configuration under which to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.visualStudioPlatformKey}">Platform:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.visualStudioPlatformKey}" className="longField"/>
            <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.visualStudioPlatformKey}" popupTitle="Select platform"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.visualStudioPlatformKey}"></span>
        <span class="smallNote">Platform under which to build.</span>
    </td>
</tr>