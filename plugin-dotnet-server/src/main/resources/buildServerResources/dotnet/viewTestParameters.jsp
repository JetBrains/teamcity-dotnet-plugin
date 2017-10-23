<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.testTestCaseFilterKey]}">
    <div class="parameter">
        Test case filter: <props:displayValue name="${params.testTestCaseFilterKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.testFrameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.testFrameworkKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.testConfigKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.testConfigKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.testOutputKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.testOutputKey}"/>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.testNoBuildKey]}">
    <div class="parameter">
        Do not build the project before testing: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.testSettingsFileKey]}">
    <div class="parameter">
        Settings file: <props:displayValue name="${params.testSettingsFileKey}"/>
    </div>
</c:if>