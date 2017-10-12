<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

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

<c:if test="${not empty propertiesBean.properties[params.packVersionSuffixKey]}">
    <div class="parameter">
        Version suffix: <props:displayValue name="${params.packVersionSuffixKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.packNoBuildKey]}">
    <div class="parameter">
        Do not build project before packing: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.packServiceableKey]}">
    <div class="parameter">
        Set the serviceable flag in the package: <strong>ON</strong>
    </div>
</c:if>