<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.publishFrameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.publishFrameworkKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.publishConfigKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.publishConfigKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.publishRuntimeKey]}">
    <div class="parameter">
        Runtime: <props:displayValue name="${params.publishRuntimeKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.publishOutputKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.publishOutputKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.publishTempKey]}">
    <div class="parameter">
        Temp directory: <props:displayValue name="${params.publishTempKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.publishVersionSuffixKey]}">
    <div class="parameter">
        Version suffix: <props:displayValue name="${params.publishVersionSuffixKey}"/>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.publishNoBuildKey]}">
    <div class="parameter">
        Do not build projects before publishing: <strong>ON</strong>
    </div>
</c:if>