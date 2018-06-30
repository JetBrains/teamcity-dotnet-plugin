<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<div class="parameter">
  Source: <strong><props:displayValue name="${params.nugetPackageSourceKey}"
                                      emptyValue="Use default source"/></strong>
</div>

<div class="parameter">
  Packages: <props:displayValue name="${params.pathsKey}"/>
</div>

<c:if test="${propertiesBean.properties[params.nugetNoSymbolsKey]}">
  <div class="parameter">
    Do not publish an existing nuget symbols package: <strong>ON</strong>
  </div>
</c:if>