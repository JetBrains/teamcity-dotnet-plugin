<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="bean" class="jetbrains.buildServer.nunit.NUnitBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<tr>
  <th class="noBorder"></th>
  <td>
    <span class="smallNote">
      Use <b>"Only if build status is successful"</b> execution policy in the following steps to prevent them from executing after test failures.
    </span>
  </td>
</tr>

<tr id="nUnitPathContainer">
  <th><label for="${bean.NUnitPathKey}">NUnit Console:<l:star/><bs:help file="NUnit" anchor="pathToNUnitConsoleTool"/></label></th>
  <td>
    <jsp:include page="/tools/selector.html?toolType=${bean.NUnitToolNameKey}&versionParameterName=${bean.NUnitPathKey}&class=${clazz}"/>
  </td>
</tr>

<props:workingDirectory/>

<tr class="advancedSetting" id="nUnitConfigFileContainer">
  <th><label for="${bean.NUnitConfigFileKey}">Path to application configuration file:<bs:help file="NUnit" anchor="appConfigFile"/></label></th>
  <td>
    <div class="posRel">
      <props:textProperty name="${bean.NUnitConfigFileKey}" className="longField">
        <jsp:attribute name="afterTextField">
          <bs:vcsTree fieldId="${bean.NUnitConfigFileKey}"/>
        </jsp:attribute>
      </props:textProperty>
    </div>
    <span class="error" id="error_${bean.NUnitConfigFileKey}"></span>
    <span class="smallNote">Specify the path to the application configuration file to be used by NUnit tests. Paths relative to the checkout directory are supported.</span>
  </td>
</tr>

<tr class="advancedSetting" id="nUnitCommandLineContainer">
  <th><label for="${bean.NUnitCommandLineKey}">Additional command line parameters:<bs:help file="NUnit" anchor="cmdParameters"/></label></th>
  <td>
    <div class="posRel">
      <props:textProperty name="${bean.NUnitCommandLineKey}" className="longField"/>
    </div>
    <span class="error" id="error_${bean.NUnitCommandLineKey}"></span>
    <span class="smallNote">Enter additional command line parameters to the NUnit console tool</span>
  </td>
</tr>

<tr>
  <th><label for="${bean.NUnitIncludeKey}">Run tests from:<l:star/></label></th>
  <td>
    <c:set var="note">Enter comma- or newline-separated paths to assembly files relative to the checkout directory. Wildcards are supported.</c:set>
    <props:multilineProperty
      name="${bean.NUnitIncludeKey}"
      className="longField"
      linkTitle="Edit assembly files include list"
      rows="3"
      cols="49"
      expanded="${true}"
      note="${note}">
    <jsp:attribute name="afterTextField">
      <c:if test="${not buildForm.template}"><bs:agentArtifactsTree fieldId="${bean.NUnitIncludeKey}" buildTypeId="${buildForm.externalId}" filesOnly="true"/></c:if>
    </jsp:attribute>
  </props:multilineProperty>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${bean.NUnitExcludeKey}">Do not run tests from:</label></th>
  <td><c:set var="note">Enter comma- or newline-separated paths to assembly files relative to the checkout directory. Wildcards are supported.</c:set>
    <props:multilineProperty
      name="${bean.NUnitExcludeKey}"
      className="longField"
      linkTitle="Edit assembly files exclude list"
      rows="3"
      cols="49"
      expanded="${not empty propertiesBean.properties[bean.NUnitExcludeKey]}"
      note="${note}"
      ><jsp:attribute name="afterTextField">
        <c:if test="${not buildForm.template}"><bs:agentArtifactsTree fieldId="${bean.NUnitExcludeKey}" buildTypeId="${buildForm.externalId}" filesOnly="true"/></c:if>
      </jsp:attribute>
    </props:multilineProperty>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${bean.NUnitCategoryIncludeKey}">Include categories:</label></th>
  <td><c:set var="note">Enter comma- or newline-separated names of NUnit categories.<bs:help file='NUnit' anchor="settings"/></c:set
      ><props:multilineProperty
      name="${bean.NUnitCategoryIncludeKey}"
      className="longField"
      linkTitle="Edit test categories include list"
      rows="3"
      cols="49"
      expanded="${not empty propertiesBean.properties[bean.NUnitCategoryIncludeKey]}"
      note="${note}"
      /></td>
</tr>

<tr class="advancedSetting">
  <th><label for="${bean.NUnitCategoryExcludeKey}">Exclude categories:</label></th>
  <td><c:set var="note">Enter comma- or newline-separated names of NUnit categories.<bs:help file='NUnit' anchor="settings"/></c:set
      ><props:multilineProperty
      name="${bean.NUnitCategoryExcludeKey}"
      className="longField"
      linkTitle="Edit test categories exclude list"
      rows="3"
      cols="49"
      expanded="${not empty propertiesBean.properties[bean.NUnitCategoryExcludeKey]}"
      note="${note}"
      /></td>
</tr>

<c:set var="runRiskGroupTestsFirstName" value="teamcity.tests.runRiskGroupTestsFirst"/>
<c:set var="runRiskGroupTestsFirstValue" value="recentlyFailed"/>
<tr class="advancedSetting">
  <th>Options:</th>
  <td>
    <props:checkboxProperty name="${runRiskGroupTestsFirstName}" value="${runRiskGroupTestsFirstValue}"/>
    <label for="${runRiskGroupTestsFirstName}">Run recently failed tests first</label>
  </td>
</tr>

<tbody id="dotnet-coverage">
<l:settingsGroup title=".NET Coverage">
  <c:if test="${propertiesBean.properties['dotNetCoverage.dotCover.enabled'] == 'true'}">
    <c:set target="${propertiesBean.properties}" property="${params.coverageTypeKey}" value="dotCover"/>
  </c:if>
  <c:set var="toolsTitle">Code Coverage:<bs:help file="Configuring+.NET+Code+Coverage"/></c:set>
  <props:selectSectionProperty name="${params.coverageTypeKey}" title="${toolsTitle}" note="">
    <props:selectSectionPropertyContent value="" caption="<No .NET Coverage>"/>
    <c:forEach items="${params.coverages}" var="type">
      <props:selectSectionPropertyContent value="${type.name}" caption="${type.description}">
        <jsp:include page="${teamcityPluginResourcesPath}/coverage/${type.editPage}"/>
      </props:selectSectionPropertyContent>
    </c:forEach>
  </props:selectSectionProperty>
</l:settingsGroup>
</tbody>