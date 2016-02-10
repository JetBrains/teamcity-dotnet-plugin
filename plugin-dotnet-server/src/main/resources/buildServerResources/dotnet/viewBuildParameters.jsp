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

<c:if test="${not empty propertiesBean.properties[params.buildArchKey]}">
    <div class="parameter">
        Architecture: <props:displayValue name="${params.buildArchKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildNativeKey]}">
    <div class="parameter">
        Compiles source to native machine code: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildCppKey]}">
    <div class="parameter">
        Make native compilation with C++ code generator: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildProfileKey]}">
    <div class="parameter">
        Print the incremental safety checks to prevent incremental compilation: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildNonIncrementalKey]}">
    <div class="parameter">
        Mark the entire build as not safe for incrementality: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildOutputKey]}">
    <div class="parameter">
        Output directory: <props:displayValue name="${params.buildOutputKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildTempKey]}">
    <div class="parameter">
        Temp directory: <props:displayValue name="${params.buildTempKey}"/>
    </div>
</c:if>