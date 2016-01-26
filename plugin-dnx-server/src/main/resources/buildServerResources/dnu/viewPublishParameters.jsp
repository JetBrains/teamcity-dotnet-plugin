<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.publishPathsKey]}">
    <div class="parameter">
        Projects: <props:displayValue name="${params.publishPathsKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.publishFrameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.publishFrameworkKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.publishConfigKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.publishConfigKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.publishRuntimeKey]}">
    <div class="parameter">
        Runtime: <props:displayValue name="${params.publishRuntimeKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.publishOutputKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.publishOutputKey}"/>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.publishNativeKey]}">
    <div class="parameter">
        Build and include native images: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.publishCompileSourcesKey]}">
    <div class="parameter">
        Compile sources into NuGet packages: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.publishIncludeSymbolsKey]}">
    <div class="parameter">
        Include symbols: <strong>ON</strong>
    </div>
</c:if>