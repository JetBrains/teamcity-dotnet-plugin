

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.inspect.InspectCodeConstantsBean"/>

<div class="parameter">
  <ul style="list-style: none; padding-left: 0; margin-left: 0; margin-top: 0.1em; margin-bottom: 0.1em;">
    <li>Solution file path: <strong><props:displayValue name="${constants.solutionPathKey}" emptyValue="not specified"/></strong></li>
  </ul>
</div>

<div class="parameter">
  <ul style="list-style: none; padding-left: 0; margin-left: 0; margin-top: 0.1em; margin-bottom: 0.1em;">
    <li>Projects to analyze: <strong><props:displayValue name="${constants.projectFilerKey}" emptyValue="all"/></strong></li>
  </ul>
</div>

<div class="parameter">
  Path to InspectCode home: <jsp:include page="/tools/selector.html?name=${constants.cltToolTypeName}&class=longField&view=1"/>
</div>

<c:if test="${not empty propertiesBean.properties[constants.cltPlatformKey]}">
  <div class="parameter">
    InspectCode platform: <strong><c:out value="${constants.getRunPlatformName(propertiesBean.properties[constants.cltPlatformKey])}"/></strong>
  </div>
</c:if>

<div class="parameter">
  <ul style="list-style: none; padding-left: 0; margin-left: 0; margin-top: 0.1em; margin-bottom: 0.1em;">
    <li>Custom settings profile path: <strong><props:displayValue name="${constants.customSettingsProfilePathKey}" emptyValue="not specified"/></strong></li>
  </ul>
</div>

<div class="parameter">
  Enable debug messages: <strong><props:displayCheckboxValue name="${constants.debugKey}"/></strong>
</div>

<div class="parameter">
  Additional InspectCode parameters: <strong><props:displayValue name="${constants.customCommandlineKey}" showInPopup="${true}"/></strong>
</div>