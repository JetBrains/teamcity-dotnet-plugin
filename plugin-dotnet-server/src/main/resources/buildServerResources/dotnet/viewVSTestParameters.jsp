<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.testNamesKey]}">
    <div class="parameter">
        Test names: <props:displayValue name="${params.testNamesKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.testCaseFilterKey]}">
    <div class="parameter">
        Test case filter: <props:displayValue name="${params.testCaseFilterKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.platformKey]}">
    <div class="parameter">
        Platform: <props:displayValue name="${params.platformKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.frameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.frameworkKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.testSettingsFileKey]}">
    <div class="parameter">
        Settings file: <props:displayValue name="${params.testSettingsFileKey}"/>
    </div>
</c:if>