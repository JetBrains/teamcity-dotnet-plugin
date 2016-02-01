<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.buildFrameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.buildFrameworkKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildConfigKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.buildConfigKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildOutputKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.buildOutputKey}"/>
    </div>
</c:if>