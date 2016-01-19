<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.buildPathsKey]}">
    <div class="parameter">
        Projects: <props:displayValue name="${params.buildPathsKey}"/>
    </div>
</c:if>

<div class="parameter">
    Framework: <props:displayValue name="${params.buildFrameworkKey}"/>
</div>

<div class="parameter">
    Configuration: <props:displayValue name="${params.buildConfigKey}"/>
</div>

<c:if test="${not empty propertiesBean.properties[params.buildOutputKey]}">
    <div class="parameter">
        Packages path: <props:displayValue name="${params.buildOutputKey}"/>
    </div>
</c:if>