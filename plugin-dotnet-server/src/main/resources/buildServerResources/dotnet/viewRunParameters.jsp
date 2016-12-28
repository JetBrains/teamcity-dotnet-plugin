<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.runFrameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.runFrameworkKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.runConfigKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.runConfigKey}"/>
    </div>
</c:if>