<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.nugetPackageSourcesKey]}">
  <div class="parameter">
    NuGet package sources: <props:displayValue name="${params.nugetPackageSourcesKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.runtimeKey]}">
  <div class="parameter">
    Runtime: <props:displayValue name="${params.runtimeKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.nugetPackagesDirKey]}">
  <div class="parameter">
    Packages directory: <props:displayValue name="${params.nugetPackagesDirKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.nugetConfigFileKey]}">
  <div class="parameter">
    Configuration file: <props:displayValue name="${params.nugetConfigFileKey}"/>
  </div>
</c:if>