<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<div class="parameter">
    SBT commands: <strong><props:displayValue name="sbt.args" emptyValue=""/></strong>
</div>
<div class="parameter">
    SBT installation mode:
    <c:choose>
        <c:when test="${propertiesBean.properties['sbt.installationMode']=='auto'}">
            <strong>Auto</strong>
            <span class="smallNote" style="margin-left: 1em !important;">TeamCity bundled SBT launcher will be used (version 0.13.8)&nbsp;<bs:help file="Simple+Build+Tool+(Scala)"/></span>
        </c:when>
        <c:when test="${propertiesBean.properties['sbt.installationMode']=='custom'}">
            <strong>Custom</strong>
            <span class="smallNote" style="margin-left: 1em !important;">sbt-launch.jar from \\bin folder under SBT home will be launched</span>
        </c:when>
        <c:otherwise>
            <props:displayValue name="sbt.installationMode"/>
        </c:otherwise>
    </c:choose>
</div>
<c:if test="${propertiesBean.properties['sbt.installationMode']=='custom'}">
    <div class="parameter">
        SBT home path: <strong><props:displayValue name="sbt.home" emptyValue="none specified"/></strong>.
    </div>
</c:if>

<props:viewWorkingDirectory/>

<props:viewJavaHome/>
<props:viewJvmArgs/>
