<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.visualStudioActionKey]}">
    <div class="parameter">
        Targets: <props:displayValue name="${params.visualStudioActionKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.visualStudioConfigKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.visualStudioConfigKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.visualStudioPlatformKey]}">
    <div class="parameter">
        Platform: <props:displayValue name="${params.visualStudioPlatformKey}"/>
    </div>
</c:if>