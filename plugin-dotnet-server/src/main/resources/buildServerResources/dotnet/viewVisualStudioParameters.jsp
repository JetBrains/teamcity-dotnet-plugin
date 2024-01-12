<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>


<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.targetsKey]}">
  <div class="parameter">
    Targets: <props:displayValue name="${params.targetsKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.configKey]}">
  <div class="parameter">
    Configuration: <props:displayValue name="${params.configKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.platformKey]}">
  <div class="parameter">
    Platform: <props:displayValue name="${params.platformKey}"/>
  </div>
</c:if>