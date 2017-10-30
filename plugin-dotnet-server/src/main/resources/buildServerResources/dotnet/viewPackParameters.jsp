<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.configKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.configKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.versionSuffixKey]}">
    <div class="parameter">
        Version suffix: <props:displayValue name="${params.versionSuffixKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.outputDirKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.outputDirKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.skipBuildKey]}">
    <div class="parameter">
        Do not build the project before packing: <strong>ON</strong>
    </div>
</c:if>