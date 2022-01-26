<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%--
  ~ Copyright 2000-2022 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

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
