<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<div class="parameter">
    Packages: <props:displayValue name="${params.pathsKey}"/>
</div>

<div class="parameter">
    Source: <strong><props:displayValue name="${params.nugetPushSourceKey}"
                                        emptyValue="Use default source"/></strong>
</div>

<c:if test="${propertiesBean.properties[params.nugetPushNoSymbolsKey]}">
    <div class="parameter">
        Do not publish existing nuget symbols package: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.nugetPushNoBufferKey]}">
    <div class="parameter">
        Disable buffering when pushing to server to decrease memory usage: <strong>ON</strong>
    </div>
</c:if>