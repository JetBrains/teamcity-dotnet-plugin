<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

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

<c:if test="${not empty propertiesBean.properties[params.testRuntimeKey]}">
    <div class="parameter">
        Runtime: <props:displayValue name="${params.testRuntimeKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.testOutputKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.testOutputKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.testTempKey]}">
    <div class="parameter">
        Temp directory: <props:displayValue name="${params.testTempKey}"/>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.testNoBuildKey]}">
    <div class="parameter">
        Do not build project before testing: <strong>ON</strong>
    </div>
</c:if>