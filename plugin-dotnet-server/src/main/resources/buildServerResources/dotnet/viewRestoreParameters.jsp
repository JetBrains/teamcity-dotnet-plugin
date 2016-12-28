<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.restoreSourceKey]}">
    <div class="parameter">
        NuGet package source: <props:displayValue name="${params.restoreSourceKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.restorePackagesKey]}">
    <div class="parameter">
        Packages path: <props:displayValue name="${params.restorePackagesKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.restoreConfigKey]}">
    <div class="parameter">
        Configuration file: <props:displayValue name="${params.restoreConfigKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.restoreParallelKey]}">
    <div class="parameter">
        Disable restoring multiple project packages in parallel: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.restoreRootProjectKey]}">
    <div class="parameter">
        Restore only root project packages: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.restoreNoCacheKey]}">
    <div class="parameter">
        Do not cache packages and http requests: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.restoreIgnoreFailedKey]}">
    <div class="parameter">
        Treat package source failures as warnings: <strong>ON</strong>
    </div>
</c:if>