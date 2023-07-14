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
      The <admin:editBuildTypeLinkFull buildType="${buildType}" cameFromUrl="${cameFromUrl}"/> build configuration employs the deprecated Duplicates Finder (ReSharper) runner that uses an incompatible version of JetBrains ReSharper Command Line Tools.
    </div>
  </c:when>
  <c:otherwise>
    <div>
      Multiple build steps of the <admin:editBuildTypeLinkFull buildType="${buildType}" cameFromUrl="${cameFromUrl}"/> build configuration employ the deprecated Duplicates Finder (ReSharper) runner that uses an incompatible version of JetBrains ReSharper Command Line Tools.
    </div>
  </c:otherwise>
</c:choose>
<div>
  The version of JetBrains ReSharper CLT selected in the Duplicates Finder (ReSharper) runner's settings does not support this runner. To continue using the runner, install JetBrains ReSharper Command Line Tools version 2021.2.3 and select this version under advanced options in the runner settings.
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
