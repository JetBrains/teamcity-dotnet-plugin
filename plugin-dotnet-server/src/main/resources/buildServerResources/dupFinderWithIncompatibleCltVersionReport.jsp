<%@ page import="jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemDisplayMode" %>
<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem" scope="request"/>
<jsp:useBean id="showMode" type="jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemDisplayMode" scope="request"/>
<jsp:useBean id="healthStatusReportUrl" type="java.lang.String" scope="request"/>
<c:set var="inplaceMode" value="<%=HealthStatusItemDisplayMode.IN_PLACE%>"/>
<c:set var="cameFromUrl" value="${showMode eq inplaceMode ? pageUrl : healthStatusReportUrl}"/>

<c:set var="buildType" value="${healthStatusItem.additionalData['buildType']}"/>
<c:set var="runnerIds" value="${healthStatusItem.additionalData['runnerIds']}"/>

<c:choose>
  <c:when test="${fn:length(runnerIds) == 1}">
    <div>
      The <admin:editBuildTypeLinkFull buildType="${buildType}" cameFromUrl="${cameFromUrl}"/> build configuration uses the Duplicates Finder (ReSharper) runner with the bundled version of JetBrains ReSharper Command Line Tools.
    </div>
  </c:when>
  <c:otherwise>
    <div>
      The <admin:editBuildTypeLinkFull buildType="${buildType}" cameFromUrl="${cameFromUrl}"/> build configuration uses multiple Duplicates Finder (ReSharper) runners with the bundled version of JetBrains ReSharper Command Line Tools.
    </div>
  </c:otherwise>
</c:choose>
<div>
  Bundled JetBrains ReSharper Command Line Tools will be upgraded in the next major TeamCity release. Since new CLT versions no longer ship the Duplicates Finder tool, the corresponding runner will cease to work. To keep the Duplicates Finder (ReSharper) runner functional, install JetBrains ReSharper Command Line Tools version 2021.2.3 and select this version under advanced options in the runner settings.
</div>
<c:if test="${not buildType.readOnly}">
  <div>
    <admin:editBuildTypeLink buildTypeId="${buildType.externalId}"
                             step="runType"
                             cameFromUrl="${cameFromUrl}">
      Edit
    </admin:editBuildTypeLink>
  </div>
</c:if>
