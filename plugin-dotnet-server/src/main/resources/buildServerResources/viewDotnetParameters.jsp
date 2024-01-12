<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>


<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<c:forEach items="${params.commands}" var="type">
  <c:if test="${propertiesBean.properties[params.commandKey] eq type.name}">
    <div class="parameter">
      Command: <strong><c:out value="${type.description}"/></strong>
    </div>

    <c:if test="${not empty propertiesBean.properties[params.pathsKey]}">
      <div class="parameter">
        Paths: <props:displayValue name="${params.pathsKey}"/>
      </div>
    </c:if>

    <jsp:include page="${teamcityPluginResourcesPath}/dotnet/${type.viewPage}"/>
  </c:if>
</c:forEach>

<c:if test="${not empty propertiesBean.properties[params.argumentsKey]}">
  <div class="parameter">
    Command line parameters: <props:displayValue name="${params.argumentsKey}"/>
  </div>
</c:if>

<c:if test="${propertiesBean.properties['dotNetCoverage.dotCover.enabled'] == 'true'}">
  <c:set target="${propertiesBean.properties}" property="${params.coverageTypeKey}" value="dotCover"/>
</c:if>
<c:forEach items="${params.coverages}" var="type">
  <c:if test="${propertiesBean.properties[params.coverageTypeKey] eq type.name}">
    <div class="parameter">
      Code Coverage: <strong><c:out value="${type.description}"/></strong>
    </div>

    <jsp:include page="${teamcityPluginResourcesPath}/coverage/${type.viewPage}"/>
  </c:if>
</c:forEach>