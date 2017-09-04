<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.cleanFrameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.cleanFrameworkKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.cleanConfigKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.cleanConfigKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.cleanRuntimeKey]}">
    <div class="parameter">
        Runtime: <props:displayValue name="${params.cleanRuntimeKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.cleanOutputKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.cleanOutputKey}"/>
    </div>
</c:if>