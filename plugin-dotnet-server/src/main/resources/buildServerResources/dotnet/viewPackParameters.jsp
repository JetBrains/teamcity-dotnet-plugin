<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.packBaseKey]}">
    <div class="parameter">
        Base directory: <props:displayValue name="${params.packBaseKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.packConfigKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.packConfigKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.packOutputKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.packOutputKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.packTempKey]}">
    <div class="parameter">
        Temp directory: <props:displayValue name="${params.packTempKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.packVersionSuffixKey]}">
    <div class="parameter">
        Version suffix: <props:displayValue name="${params.packVersionSuffixKey}"/>
    </div>
</c:if>