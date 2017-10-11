<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<div class="parameter">
    Package: <props:displayValue name="${params.nugetDeleteIdKey}"/>
</div>

<div class="parameter">
    Source: <strong><props:displayValue name="${params.nugetDeleteSourceKey}"
                                        emptyValue="Use default source"/></strong>
</div>