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

<c:if test="${not empty propertiesBean.properties[params.runtimeKey]}">
  <div class="parameter">
    Runtime: <props:displayValue name="${params.runtimeKey}"/>
  </div>
</c:if>