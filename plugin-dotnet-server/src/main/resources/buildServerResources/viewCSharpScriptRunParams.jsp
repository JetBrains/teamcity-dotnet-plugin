<%--
  ~ Copyright (c) 2006, JetBrains, s.r.o. All Rights Reserved.
  --%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="propertyNames" class="jetbrains.buildServer.script.CSharpScriptConstantsBean"/>

<props:viewWorkingDirectory />

<div class="parameter">
  C# script: <props:displayValue name="${propertyNames.scriptContent}" emptyValue="<empty>" showInPopup="true" popupTitle="Script content" popupLinkText="view script content"/>
</div>
