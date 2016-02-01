<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.packFrameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.packFrameworkKey}"/>
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