<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DnxParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.pathsKey]}">
    <div class="parameter">
        Project: <props:displayValue name="${params.pathsKey}"/>
    </div>
</c:if>

<div class="parameter">
    Command: <props:displayValue name="${params.commandKey}"/>
</div>

<c:if test="${not empty propertiesBean.properties[params.frameworkKey]}">
    <div class="parameter">
        Framework: <props:displayValue name="${params.frameworkKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.configKey]}">
    <div class="parameter">
        Configuration: <props:displayValue name="${params.configKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.appbaseKey]}">
    <div class="parameter">
        Base directory: <props:displayValue name="${params.appbaseKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.libsKey]}">
    <div class="parameter">
        Libraries directory: <props:displayValue name="${params.libsKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.packagesKey]}">
    <div class="parameter">
        Packages directory: <props:displayValue name="${params.packagesKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.argumentsKey]}">
    <div class="parameter">
        Command line parameters: <props:displayValue name="${params.argumentsKey}"/>
    </div>
</c:if>
