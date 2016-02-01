<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.restorePackagesKey]}">
    <div class="parameter">
        Packages path: <props:displayValue name="${params.restorePackagesKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.restoreParallelKey]}">
    <div class="parameter">
        Parallel execution for multiple discovered projects: <strong>ON</strong>
    </div>
</c:if>