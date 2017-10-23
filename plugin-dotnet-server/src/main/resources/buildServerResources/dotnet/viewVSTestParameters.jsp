<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.vstestTestNamesKey]}">
    <div class="parameter">
        Test names: <props:displayValue name="${params.vstestTestNamesKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.vstestTestCaseFilterKey]}">
    <div class="parameter">
        Test case filter: <props:displayValue name="${params.vstestTestCaseFilterKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.vstestPlatformKey]}">
    <div class="parameter">
        Platform: <props:displayValue name="${params.vstestPlatformKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.vstestFrameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.vstestFrameworkKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.vstestInIsolationKey]}">
    <div class="parameter">
        Run in isolation: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.vstestSettingsFileKey]}">
    <div class="parameter">
        Settings file: <props:displayValue name="${params.vstestSettingsFileKey}"/>
    </div>
</c:if>