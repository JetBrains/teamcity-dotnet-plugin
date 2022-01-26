<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
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

<tr class="advancedSetting">
  <th>dotCover CLT home directory:</th>
  <td>
    <jsp:include page="/tools/selector.html?toolType=JetBrains.dotCover.CommandLineTools&versionParameterName=${params.dotCoverHomeKey}&class=longField"/>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${params.dotCoverFiltersKey}">Assembly filters:</label></th>
  <td>
    <c:set var="note">
      New-line separated list of assemblies to be included/exluded from the code coverage
      following the <i>+:myassemblyName</i> or <i>-:myassemblyName</i> syntax (use the name only,
      without the extension). The (*) wildcard is supported.<bs:help file="JetBrains+dotCover"/>
    </c:set>
    <props:multilineProperty name="${params.dotCoverFiltersKey}" className="longField" expanded="true" cols="60" rows="4" linkTitle="Assembly filters" note="${note}"/>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${params.dotCoverAttributeFiltersKey}">Attribute filters:</label></th>
  <td>
    <c:set var="note">
      New-line separated list of attribute filters following the <i>-:attributeName</i> syntax
      to exclude the code marked with attributes from code coverage.
      The (*) wildcard is supported.<bs:help file="JetBrains+dotCover"/>
    </c:set>
    <props:multilineProperty name="${params.dotCoverAttributeFiltersKey}" className="longField" cols="60" rows="4" linkTitle="Attribute filters" note="${note}"/>
    <span class="smallNote"><strong>Applicable to dotCover 2.0 or higher.</strong></span>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${params.dotCoverArgumentsKey}">Additional arguments:</label></th>
  <td>
    <props:multilineProperty name="${params.dotCoverArgumentsKey}" linkTitle="Additional arguments" cols="60" rows="5"/>
    <span class="smallNote">Additional new-line separated command line parameters for dotCover.</span>
    <span id="error_${params.dotCoverArgumentsKey}" class="error"></span>
  </td>
</tr>