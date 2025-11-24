<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotCover.DotCoverRunnerParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<c:if test="${not empty propertiesBean.properties[params.dotCoverCoveredProcessExecutableKey]}">
  <div class="parameter">
    Executable: <props:displayValue name="${params.dotCoverCoveredProcessExecutableKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.dotCoverCoveredProcessArgumentsKey]}">
  <div class="parameter">
    Command line arguments: <props:displayValue name="${params.dotCoverCoveredProcessArgumentsKey}"/>
  </div>
</c:if>

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

<c:if test="${not empty propertiesBean.properties[params.dotCoverGenerateReportKey]}">
  <div class="parameter">
    Generate coverage report: <props:displayValue name="${params.dotCoverGenerateReportKey}"/>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.dotCoverAdditionalShapshotPathsKey]}">
  <div class="parameter">
    Additional dotCover snapshot paths: <props:displayValue name="${params.dotCoverAdditionalShapshotPathsKey}"/>
  </div>
</c:if>
