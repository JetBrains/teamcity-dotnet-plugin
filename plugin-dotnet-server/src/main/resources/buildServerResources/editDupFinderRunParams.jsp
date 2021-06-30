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

<jsp:useBean id="constants" class="jetbrains.buildServer.inspect.DupFinderConstantsBean"/>

<l:settingsGroup title="Sources">
  <tr>
    <th>
      <label for="${constants.includeFilesKey}">Include:</label>
    </th>
    <td>
      <span>
        <c:set var="note">Newline-delimited Ant-like wildcards relative to the checkout root are supported.<br/>
          Visual Studio solution files are parsed and replaced by all source files from all projects within a solution.<br/>
          Example: src\MySolution.sln
        </c:set>
        <props:multilineProperty name="${constants.includeFilesKey}"
                                 linkTitle="Type file wildcards or solution file name"
                                 cols="55" rows="5"
                                 note="${note}"
                                 expanded="true"/>
      </span>
    </td>
  </tr>

  <tr>
    <th>
      <label for="${constants.excludeFilesKey}">Exclude:</label>
    </th>
    <td>
      <span>
        <props:multilineProperty name="${constants.excludeFilesKey}"
                                 linkTitle="Exclude files by wildcard"
                                 note="Newline-delimited Ant-like wildcards relative to the checkout root are supported.<br/>Example: **/*generated*.cs"
                                 cols="55" rows="5"/>
      </span>
    </td>
  </tr>

</l:settingsGroup>

<jsp:include page="/tools/editToolUsage.html?toolType=${constants.cltToolTypeName}&versionParameterName=${constants.cltPathKey}&class=longField"/>

<tr>
  <th><label for="${constants.cltPlatformKey}">dupFinder Platform:  <bs:help file="inspections-resharper#ReSharperDupFinderPlatform" anchor=""/></label></th>
  <td>
    <props:selectProperty name="${constants.cltPlatformKey}" enableFilter="true" className="mediumField">
      <c:forEach var="item" items="${constants.runPlatforms}">
        <props:option value="${item}"><c:out value="${item}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${constants.cltPlatformKey}"></span>
  </td>
</tr>


<l:settingsGroup title="Duplicate Searcher Settings" className="advancedSetting">

  <tr class="advancedSetting">
    <th>
      <label>Code fragments comparison: <bs:help file="duplicates-finder-resharper#fragComp" anchor=""/></label>
    </th>
    <td>
      <props:checkboxProperty name="${constants.normalizeTypesKey}"/>
      <label for="${constants.normalizeTypesKey}">Discard namespaces</label>
      <br/>
      <props:checkboxProperty name="${constants.discardTypesKey}"/>
      <label for="${constants.discardTypesKey}">Discard types name</label>
      <br/>
      <props:checkboxProperty name="${constants.discardFieldsNameKey}"/>
      <label for="${constants.discardFieldsNameKey}">Discard class fields name</label>
      <br/>
      <props:checkboxProperty name="${constants.discardLocalVariablesNameKey}"/>
      <label for="${constants.discardLocalVariablesNameKey}">Discard local variables name</label>
      <br/>
      <props:checkboxProperty name="${constants.discardLiteralsKey}"/>
      <label for="${constants.discardLiteralsKey}">Discard literals</label>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${constants.discardCostKey}">Ignore duplicates with complexity lower than:</label></th>
    <td>
      <props:textProperty name="${constants.discardCostKey}" className="longField"/>
      <span class="error" id="error_${constants.discardCostKey}"></span>
      <span class="smallNote">Positive numbers and parameter references are supported.</span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th>
      <label for="${constants.excludeByOpeningCommentKey}">Skip files by opening comment:</label>
    </th>
    <td>
      <span>
        <props:multilineProperty name="${constants.excludeByOpeningCommentKey}"
                                 linkTitle="Enter comment substrings"
                                 note="Newline-delimited comment substrings are supported."
                                 cols="55" rows="5"/>
      </span>
    </td>
  </tr>
  <%----%>
  <tr class="advancedSetting">
    <th>
      <label for="${constants.excludeRegionMessageSubstringsKey}">Skip regions by message substring:</label>
    </th>
    <td>
      <span>
        <props:multilineProperty name="${constants.excludeRegionMessageSubstringsKey}"
                                 linkTitle="Enter region message substrings"
                                 note="Newline-delimited region message substrings are supported.<br/>This setting allows skipping the GUI designer regions."
                                 cols="55" rows="5"/>
      </span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th>
      <label for="${constants.debugKey}">Enable debug output: <bs:help file="duplicates-finder-resharper#debug" anchor=""/></label>
    </th>
    <td>
      <props:checkboxProperty name="${constants.debugKey}"/>
      <span class="error" id="error_${constants.debugKey}"></span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${constants.customCommandlineKey}">Additional dupFinder parameters: <bs:help file="duplicates-finder-resharper#cmdArgs" anchor=""/></label></th>
    <td>
      <props:multilineProperty name="${constants.customCommandlineKey}" linkTitle="Command line parameters" cols="60" rows="5" note="Additional command line parameters for dupFinder separated by new lines."/>
    </td>
  </tr>

</l:settingsGroup>

<l:settingsGroup title="Build Failure Conditions">

<tr>
  <th colspan="2">You can configure a build to fail if it has too many duplicates. To do so, add a corresponding
    <c:set var="editFailureCondLink"><admin:editBuildTypeLink step="buildFailureConditions" buildTypeId="${buildForm.settings.externalId}" withoutLink="true"/></c:set>
    <a href="${editFailureCondLink}#addFeature=BuildFailureOnMetric">build failure condition</a>.
  </th>
</tr>

</l:settingsGroup>
