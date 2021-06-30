<%--
  ~ Copyright 2000-2010 JetBrains s.r.o.
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

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="constants" class="jetbrains.buildServer.inspect.InspectCodeConstantsBean"/>

<l:settingsGroup title="Sources to Analyze">

  <tr>
    <th><label for="${constants.solutionPathKey}">Solution file path: <l:star/></label></th>
    <td>
      <props:textProperty name="${constants.solutionPathKey}" className="longField">
        <jsp:attribute name="afterTextField"><bs:vcsTree fieldId="${constants.solutionPathKey}" treeId="${constants.solutionPathKey}"/></jsp:attribute>
      </props:textProperty>
      <span class="error" id="error_${constants.solutionPathKey}"></span>
      <span class="smallNote">Specified path should be relative to the checkout directory.</span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th>
      <label for="${constants.projectFilerKey}">Projects filter:</label>
    </th>
    <td>
      <span>
        <c:set var="note">
          Analyze only projects selected by provided wildcards separated by new lines.<br/>
          Leave blank to analyze whole solution.<br/>
          Example: JetBrains.CommandLine.Tests.*
        </c:set>
        <props:multilineProperty name="${constants.projectFilerKey}"
                                 linkTitle="Project name wildcards" note="${note}"
                                 cols="55" rows="5"/>
      </span>
    </td>
  </tr>

</l:settingsGroup>

<l:settingsGroup title="Environment Requirements" className="advancedSetting">

  <tr class="advancedSetting">
    <th>
      <label>Target Frameworks: <bs:help file="inspections-resharper#targetFramework" anchor=""/></label>
    </th>
    <td>
      <c:forEach var="target" items="${constants.availableTargetFrameworks}">
        <props:checkboxProperty name="${target.id}"/>
        <label for="${target.id}">${target.description}</label>
        <br/>
      </c:forEach>
      <span class="smallNote" style="padding-top: 8px">
        Specify all versions of .NET Framework Target Pack which are required to build your project.<br/>
      </span>
    </td>
  </tr>

</l:settingsGroup>

<jsp:include page="/tools/editToolUsage.html?toolType=${constants.cltToolTypeName}&versionParameterName=${constants.cltPathKey}&class=longField"/>

<tr class="advancedSetting">
  <th><label for="${constants.cltPluginsKey}">R# CLT Plugins:</label></th>
  <td>
    <c:set var="note">
      Newline-delimited list of <a href="https://resharper-plugins.jetbrains.com/" target="_blank">ReSharper plugins</a> required for InspectCode in the following format:
      <ul style="margin: 0; padding-left: 1.5em;">
        <li>Download %pluginId%/%version%</li>
        <li>File %filePath%</li>
        <li>Folder %folderPath%</li>
      </ul>
      To download plugins add the <em>https://resharper-plugins.jetbrains.com/api/v2/</em> NuGet package source.
    </c:set>
    <props:multilineProperty name="${constants.cltPluginsKey}" linkTitle="Plugins list" cols="60" rows="5" note="${note}"/>
  </td>
</tr>

<tr>
  <th><label for="${constants.cltPlatformKey}">InspectCode Platform: <bs:help file="inspections-resharper#ReSharperInspectionsPlatform" anchor=""/></label></th>
  <td>
    <props:selectProperty name="${constants.cltPlatformKey}" enableFilter="true" className="mediumField">
      <c:forEach var="item" items="${constants.runPlatforms}">
        <props:option value="${item}"><c:out value="${item}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${constants.cltPlatformKey}"></span>
    <span class="smallNote">To find code issues in C++ projects, use the x86 platform.</span>
  </td>
</tr>

<l:settingsGroup title="InspectCode Options" className="advancedSetting">

  <tr class="advancedSetting">
    <th><label for="${constants.customSettingsProfilePathKey}">Custom settings profile path: <bs:help file="inspections-resharper#settings" anchor=""/></label></th>
    <td>
      <props:textProperty name="${constants.customSettingsProfilePathKey}" className="longField">
         <jsp:attribute name="afterTextField"><bs:vcsTree fieldId="${constants.customSettingsProfilePathKey}" treeId="${constants.customSettingsProfilePathKey}"/></jsp:attribute>
      </props:textProperty>
      <span class="error" id="error_${constants.customSettingsProfilePathKey}"></span>
      <span class="smallNote">
        Leave blank to use build-in <a href="http://www.jetbrains.com/resharper/">JetBrains ReSharper</a> settings.<br>
        Specified path should be relative to the checkout directory.
      </span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th>
      <label for="${constants.debugKey}">Enable debug output: <bs:help file="inspections-resharper#debug" anchor=""/></label>
    </th>
    <td>
      <props:checkboxProperty name="${constants.debugKey}"/>
      <span class="error" id="error_${constants.debugKey}"></span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${constants.customCommandlineKey}">Additional InspectCode parameters: <bs:help file="inspections-resharper#cmdArgs" anchor=""/></label></th>
    <td>
      <props:multilineProperty name="${constants.customCommandlineKey}" linkTitle="Command line parameters" cols="60" rows="5" note="Additional command line parameters for InspectCode separated by new lines."/>
    </td>
  </tr>

</l:settingsGroup>

<l:settingsGroup title="Build Failure Conditions">

<tr>
  <th colspan="2">You can configure a build to fail if it has too many inspection errors or warnings. To do so, add a corresponding
    <c:set var="editFailureCondLink">
      <c:choose>
        <%--@elvariable id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.EditableBuildTypeSettingsForm"--%>
        <c:when test="${not empty buildForm.settingsTemplate}">
          <admin:editTemplateLink step="buildFailureConditions" templateId="${buildForm.settingsTemplate.externalId}" withoutLink="true"/>
        </c:when>
        <c:otherwise>
          <admin:editBuildTypeLink step="buildFailureConditions" buildTypeId="${buildForm.settingsBuildType.externalId}" withoutLink="true"/>
        </c:otherwise>
      </c:choose>
    </c:set>
    <a href="${editFailureCondLink}#addFeature=BuildFailureOnMetric">build failure condition</a>.
  </th>
</tr>

</l:settingsGroup>
