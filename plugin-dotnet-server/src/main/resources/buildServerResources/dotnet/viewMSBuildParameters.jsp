<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.msbuildTargetsKey]}">
    <div class="parameter">
        Targets: <props:displayValue name="${params.msbuildTargetsKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.msbuildConfigKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.msbuildConfigKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.msbuildPlatformKey]}">
    <div class="parameter">
        Platform: <props:displayValue name="${params.msbuildPlatformKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.msbuildRuntimeKey]}">
    <div class="parameter">
        Runtime: <props:displayValue name="${params.msbuildRuntimeKey}"/>
    </div>
</c:if>