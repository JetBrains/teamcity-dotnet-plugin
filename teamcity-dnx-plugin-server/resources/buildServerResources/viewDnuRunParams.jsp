<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<div class="parameter">
    Command: <props:displayValue name="${params.commandKey}"/>
</div>

<c:if test="${not empty propertiesBean.properties[params.projectPathsKey]}">
    <div class="parameter">
        Projects: <props:displayValue name="${params.projectPathsKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.packagePathsKey]}">
    <div class="parameter">
        Packages path: <props:displayValue name="${params.packagePathsKey}"/>
    </div>
</c:if>

<div class="parameter">
    Parallel execution for multiple discovered projects:
    <strong>${propertiesBean.properties[params.parallelExecutionKey] ? 'ON' : 'OFF'}</strong>
</div>

<c:if test="${not empty propertiesBean.properties[params.argumentsKey]}">
    <div class="parameter">
        Command line parameters: <props:displayValue name="${params.argumentsKey}"/>
    </div>
</c:if>
