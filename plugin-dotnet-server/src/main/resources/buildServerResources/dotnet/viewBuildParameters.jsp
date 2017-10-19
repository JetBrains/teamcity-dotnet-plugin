<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

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

<c:if test="${not empty propertiesBean.properties[params.buildRuntimeKey]}">
    <div class="parameter">
        Runtime: <props:displayValue name="${params.buildRuntimeKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildNonIncrementalKey]}">
    <div class="parameter">
        Turn off incremental build: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildNoDependenciesKey]}">
    <div class="parameter">
        Ignore project-to-project references and only build the root project: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildOutputKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.buildOutputKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildVersionSuffixKey]}">
    <div class="parameter">
        Version suffix: <props:displayValue name="${params.buildVersionSuffixKey}"/>
    </div>
</c:if>