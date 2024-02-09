<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>


<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.dotCoverFiltersKey]}">
  <div class="parameter">
    Filters: <props:displayValue name="${params.dotCoverFiltersKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.dotCoverAttributeFiltersKey]}">
  <div class="parameter">
    Attribute filters: <props:displayValue name="${params.dotCoverAttributeFiltersKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.dotCoverArgumentsKey]}">
  <div class="parameter">
    Additional arguments: <props:displayValue name="${params.dotCoverArgumentsKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.dotCoverReportKey]}">
  <div class="parameter">
    Generate coverage report: <props:displayValue name="${params.dotCoverReportKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.dotCoverMergeKey]}">
  <div class="parameter">
    Join reports from previous build steps: <props:displayValue name="${params.dotCoverMergeKey}"/>
  </div>
</c:if>