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
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<c:set var="asterisk"><l:star/></c:set>
<c:set var="paramHelpUrl">net#</c:set>

<script type="text/javascript">
  BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}dotnet-settings.css'/>");

  var commandId = BS.Util.escapeId('${params.commandKey}');
  var clearPropertiesSkipList = [
      "prop:dotNetCoverage.dotCover.home.path"
  ];

  BS.DotnetParametersForm = {
    projectArtifactsSelector: [],
    coverageEnabled: [],
    hideLogging: [],
    helpUrl: [],
    hideWorkingDirectory: [],
    mandatoryPaths: [],
    initFunctions: [],
    selectProjectFile: function (chosenFile) {
      var $paths = $j(BS.Util.escapeId('${params.pathsKey}'));
      var value = BS.Util.trimSpaces($paths.val());

      chosenFile = chosenFile.indexOf(" ") >= 0 ? '"' + chosenFile + '"' : chosenFile;
      $paths.val(value.length > 0 ? value + " " + chosenFile : chosenFile);
      BS.MultilineProperties.updateVisible();
    },
    pathName: [],
    pathHint: [],
    clearInputValues: function(row) {
      $j(row).find(':input').each(function(id, element) {
        var $element = $j(element);
        var name = $element.attr("name");
        if (!name || name.indexOf("prop:") !== 0 || clearPropertiesSkipList.indexOf(name) >= 0) {
          return;
        }
        var changed = false;
        if (element.name === "select") {
          changed = element.selectedIndex !== -1;
          element.selectedIndex = -1;
        } else if (element.type === "checkbox") {
          changed = $element.is(':checked');
          $element.removeAttr('checked');
        } else {
          changed = $element.val() !== '';
          $element.val('');
        }
        if (changed) {
          $element.change();
        }
      });
    },
    updateElements: function () {
      var commandName = $j(commandId).val();

      var pathsName = BS.DotnetParametersForm.pathName[commandName];
      var pathsRow = $j(BS.Util.escapeId('${params.pathsKey}-row'));
      if (pathsName) {
        var label = pathsRow.find("label");
        label.text(pathsName + ':');
        if (BS.DotnetParametersForm.mandatoryPaths[commandName]) {
          label.append(' ').append('${asterisk}');
        }

        var hint = BS.DotnetParametersForm.pathHint[commandName];
        $j(BS.Util.escapeId('${params.pathsKey}-hint')).text(hint);
        var artifactsSelector = BS.DotnetParametersForm.projectArtifactsSelector[commandName];
        pathsRow.find('.vcsTreeSources').toggleClass('hidden', !!artifactsSelector);
        pathsRow.find('.vcsTreeFiles').toggleClass('hidden', !artifactsSelector);

        pathsRow.removeClass("hidden");
      } else {
        pathsRow.addClass("hidden");
        BS.DotnetParametersForm.clearInputValues(pathsRow);
      }

      $j("div.wizzard").each(function(id, element) {
        var $wizzard = $j(element);
        var $wizzardElements = $wizzard.find('span.icon-magic');
        if ($wizzardElements.length == 1) {
          $wizzardElements[0].hidden = !$wizzard.hasClass(commandName);
        }
      });

      $j("tr.dotnet").each(function(id, element) {
        var $row = $j(element);
        if (!$row.hasClass(commandName)) {
          $row.addClass("hidden");
          BS.DotnetParametersForm.clearInputValues($row);
        } else {
          $row.removeClass("hidden");
        }
      });
      $j(".runnerFormTable span.error").empty();

      var hideLogging = BS.DotnetParametersForm.hideLogging[commandName];
      var loggingRow = $j(BS.Util.escapeId('logging'))
      loggingRow.toggleClass('hidden', !!hideLogging);

      var coverageEnabled = BS.DotnetParametersForm.coverageEnabled[commandName];
      var $coverageRow = $j(BS.Util.escapeId('dotnet-coverage'));
      $coverageRow.toggleClass('hidden', !coverageEnabled);
      if (!coverageEnabled) {
          BS.DotnetParametersForm.clearInputValues($coverageRow);
      }

      var helpUrl = BS.DotnetParametersForm.helpUrl[commandName];
      $j(BS.Util.escapeId('dotnet-help')).attr('href', helpUrl);

      var hideWorkingDirectory = BS.DotnetParametersForm.hideWorkingDirectory[commandName];
      var $workingDir = $j(BS.Util.escapeId('teamcity.build.workingDir'));
      $workingDir.closest('tr').toggleClass('hidden', !!hideWorkingDirectory);
      if (hideWorkingDirectory) {
          $workingDir.val('');
      }

      var commandRow = $j(BS.Util.escapeId('commandLine'));
      if (commandName == "custom") {
        commandRow.removeClass("advancedSetting");
        commandRow.removeClass("advanced_hidden")
        commandRow.removeClass("advancedSettingHighlight")
      } else {
        commandRow.addClass("advancedSetting");
        if (loggingRow.hasClass("advanced_hidden")) {
          commandRow.addClass("advanced_hidden");
        }

        if (loggingRow.hasClass("advancedSettingHighlight")) {
          commandRow.addClass("advancedSettingHighlight");
        }
      }

      var init = BS.DotnetParametersForm.initFunctions[commandName];
      if (init) init();

      BS.MultilineProperties.updateVisible();
    },
    getFeedUrlQueryString: function () {
      var parameters = {
        authTypes: "httpAuth;guestAuth",
        apiVersions: "v3"
      };
      var search = window.location.search.substring(1).split('&');
      search.forEach(function (value) {
        var buildTypeMatch = value.match(/id=buildType:(.*)/);
        if (buildTypeMatch) {
          parameters["buildType"] = buildTypeMatch[1]
        }
        var templateMatch = value.match(/id=template:(.*)/);
        if (templateMatch) {
          parameters["template"] = templateMatch[1]
        }
      });

      return Object.keys(parameters).reduce(function (previous, key) {
        if (previous) {
          previous += "&";
        }
        return previous + key + "=" + parameters[key];
      }, "");
    }
  };

  $j(document).on('change', commandId, function () {
    BS.DotnetParametersForm.updateElements();
  });

  $j(document).on('ready', commandId, function () {
    BS.DotnetParametersForm.updateElements();
  });
</script>

<tr class="dotnet test vstest msbuild">
  <th class="noBorder"></th>
  <td>
    <span class="smallNote">
    Successful step execution is reported when all the tests have run even if some have failed. Use failure conditions to fail the build on test failures. Use <b>"Only if build status is successful"</b> execution policy in the following steps to prevent them from executing after test failures.
    </span>
  </td>
</tr>

<c:if test="${params.experimentalMode == true}">
  <tr class="advancedSetting">
    <th><label>Experimental Mode: </label></th>
    <td>
      <label>on</label>
    </td>
  </tr>
</c:if>

<c:set var="commandTitle">Command:<bs:help file="${paramHelpUrl}BuildRunnerOptions"/></c:set>
<props:selectSectionProperty name="${params.commandKey}" title="${commandTitle}" note="">
  <c:forEach items="${params.commands}" var="type">
    <props:selectSectionPropertyContent value="${type.name}" caption="${type.description}">
      <jsp:include page="${teamcityPluginResourcesPath}/dotnet/${type.editPage}"/>
    </props:selectSectionPropertyContent>
  </c:forEach>
</props:selectSectionProperty>

<tr id="${params.pathsKey}-row" class="build clean custom">
  <th class="noBorder"><label for="${params.pathsKey}">Projects:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.pathsKey}" className="longField" expandable="true"/>
      <div class="vcsTreeSources">
        <bs:vcsTree treeId="${params.pathsKey}" callback="BS.DotnetParametersForm.selectProjectFile"/>
      </div>
      <div class="vcsTreeFiles hidden">
        <c:if test="${not buildForm.template}">
          <bs:agentArtifactsTree fieldId="${params.pathsKey}" buildTypeId="${buildForm.externalId}" filesOnly="true"/>
        </c:if>
      </div>
    </div>
    <span class="error" id="error_${params.pathsKey}"></span>
    <span class="smallNote">
            <span id="${params.pathsKey}-hint">Specify target files separated by spaces or new lines</span>.
            <bs:helpLink file="Wildcards">Wildcards</bs:helpLink> are supported.
        </span>
  </td>
</tr>

<props:workingDirectory/>

<tr class="dotnet devenv">
  <th><label for="${params.visualStudioActionKey}">Build action:<bs:help file="${paramHelpUrl}devenv-build-action"/> <l:star/></label></th>
  <td>
    <props:selectProperty name="${params.visualStudioActionKey}" enableFilter="true" className="mediumField">
      <props:option value="">&lt;Select&gt;</props:option>
      <c:forEach var="item" items="${params.visualStudioActions}">
        <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${params.visualStudioActionKey}"></span>
    <span class="smallNote">Select a devenv command.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet msbuild">
  <th class="noBorder"><label for="${params.msbuildVersionKey}">MSBuild version:<bs:help file="${paramHelpUrl}msbuild-version"/></label></th>
  <td>
    <props:selectProperty name="${params.msbuildVersionKey}" enableFilter="true" className="mediumField">
      <c:forEach var="item" items="${params.msbuildVersions}">
        <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${params.msbuildVersionKey}"></span>
  </td>
</tr>

<tr class="dotnet vstest">
  <th class="noBorder"><label for="${params.vstestVersionKey}">VSTest version:<bs:help file="${paramHelpUrl}vstest-version"/></label></th>
  <td>
    <props:selectProperty name="${params.vstestVersionKey}" enableFilter="true" className="mediumField">
      <c:forEach var="item" items="${params.vstestVersions}">
        <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${params.vstestVersionKey}"></span>
  </td>
</tr>

<tr class="dotnet devenv">
  <th class="noBorder"><label for="${params.visualStudioVersionKey}">Visual Studio version:</label></th>
  <td>
    <props:selectProperty name="${params.visualStudioVersionKey}" enableFilter="true" className="mediumField">
      <c:forEach var="item" items="${params.visualStudioVersions}">
        <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${params.visualStudioVersionKey}"></span>
    <span class="smallNote" id="defaultNote_${params.visualStudioVersionKey}">Select a version of Visual Studio to run. Leave &lt;Any&gt; to use the latest installed version.</span>
  </td>
</tr>

<tr class="dotnet vstest">
  <th class="noBorder"><label for="${params.platformKey}">Platform:<bs:help file="${paramHelpUrl}vstest-platform"/></label></th>
  <td>
    <props:selectProperty name="${params.platformKey}" enableFilter="true" className="mediumField">
      <c:forEach var="item" items="${params.vstestPlatforms}">
        <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${params.platformKey}"></span>
    <span class="smallNote">Change the target platform for testing if necessary. Leave &lt;Auto&gt; to use the platform selected by VSTest.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet vstest">
  <th class="noBorder"><label for="${params.vstestInIsolation}">Run in isolation:<bs:help file="${paramHelpUrl}vstest-isolation"/></label></th>
  <td>
    <props:checkboxProperty name="${params.vstestInIsolation}"/>
    <label for="${params.vstestInIsolation}">Run the tests in an isolated process</label>
    <br/>
    <props:checkboxProperty name="${params.singleSessionKey}"/>
    <label for="${params.singleSessionKey}">Run tests in a single session</label>
    <span class="smallNote">A test session must include assemblies related to a single target framework.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet vstest">
  <th class="noBorder"><label for="${params.testFilterKey}">Tests filtration:</label></th>
  <td>
    <props:selectProperty name="${params.testFilterKey}" enableFilter="true" className="mediumField">
      <props:option value="">&lt;Disabled&gt;</props:option>
      <props:option value="name">Test names</props:option>
      <props:option value="filter">Test case filter</props:option>
    </props:selectProperty>
  </td>
</tr>

<c:set var="testNamesHint">
  Run tests with names that match the values in the provided newline-separated list.
  <bs:help urlPrefix="https://msdn.microsoft.com/en-us/library/jj155796.aspx" file=""/>
</c:set>
<tr class="advancedSetting dotnet vstest">
  <th class="noBorder">
    <label for="${params.testNamesKey}">Test names: <l:star/></label>
  </th>
  <td>
    <props:multilineProperty expanded="true" name="${params.testNamesKey}" className="longField"
                             rows="3" cols="49" linkTitle="Edit test names"
                             note="${testNamesHint}"/>
  </td>
</tr>

<tr class="advancedSetting dotnet test vstest">
  <th>
    <label for="${params.testCaseFilterKey}">Test case filter:</label>
  </th>
  <td>
    <props:textProperty name="${params.testCaseFilterKey}" className="longField"/>
    <span class="error" id="error_${params.testCaseFilterKey}"></span>
    <span class="smallNote">
            Run tests that match the given expression.
            <bs:help urlPrefix="https://msdn.microsoft.com/en-us/library/jj155796.aspx" file=""/>
        </span>
  </td>
</tr>

<tr class="advancedSetting dotnet build clean publish run test vstest">
  <th class="noBorder"><label for="${params.frameworkKey}">Framework:</label></th>
  <td>
    <div class="position-relative wizzard build clean publish run test">
      <props:textProperty name="${params.frameworkKey}" className="longField"/>
      <bs:projectData type="DotnetFrameworks" sourceFieldId="${params.pathsKey}"
                      targetFieldId="${params.frameworkKey}" popupTitle="Select frameworks"
                      selectionMode="single"/>
    </div>
    <span class="error" id="error_${params.frameworkKey}"></span>
    <span class="smallNote">Target .NET Framework version to be used for test execution.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet build pack publish restore test run nuget-push nuget-delete clean msbuild vstest testassembly devenv custom">
  <th class="noBorder"><label for="${params.requiredSdkKey}">Required SDK:</label></th>
  <td>
    <div class="position-relative wizzard build pack publish restore test run clean msbuild devenv">
      <props:textProperty name="${params.requiredSdkKey}" className="longField"/>
      <bs:projectData type="DotnetSdk" sourceFieldId="${params.pathsKey}"
                      targetFieldId="${params.requiredSdkKey}" popupTitle="Select SDK or targeting pack versions"
                      selectionMode="multiple"/>
    </div>
    <span class="error" id="error_${params.requiredSdkKey}"></span>
    <span class="smallNote">Enter space-separated SDK or targeting pack versions to be required on build agents.<br/>For example, <i>4.7.2 4.8 5</i>.<bs:help file="${paramHelpUrl}requiredNetSDK"/></span>
  </td>
</tr>

<tr class="advancedSetting dotnet msbuild">
  <th class="noBorder"><label for="${params.targetsKey}">Targets:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.targetsKey}" className="longField"/>
      <bs:projectData type="DotnetTargets" sourceFieldId="${params.pathsKey}"
                      targetFieldId="${params.targetsKey}" popupTitle="Select targets"/>
    </div
    <span class="error" id="error_${params.targetsKey}"></span>
    <span class="smallNote">Enter targets separated by space or semicolon.</span>
  </td>
</tr>

<tr class="dotnet build clean msbuild pack publish run test devenv">
  <th class="noBorder"><label for="${params.configKey}">Configuration:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.configKey}" className="longField"/>
      <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                      targetFieldId="${params.configKey}" popupTitle="Select configuration"
                      selectionMode="single"/>
    </div>
    <span class="error" id="error_${params.configKey}"></span>
    <span class="smallNote">Specify the target configuration.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet restore">
  <th class="noBorder"><label for="${params.nugetPackageSourcesKey}">NuGet package sources:</label></th>
  <td>
    <props:multilineProperty name="${params.nugetPackageSourcesKey}" className="longField" expanded="true"
                             cols="60" rows="3" linkTitle=""/>
    <%--<bs:projectData type="NuGetFeedUrls" sourceFieldId="queryString"
                    targetFieldId="${params.nugetPackageSourcesKey}" popupTitle="Select TeamCity NuGet feeds"/>--%>
    <span class="error" id="error_${params.nugetPackageSourcesKey}"></span>
    <span class="smallNote">
        Leave blank to use NuGet.org<br />
        To use a TeamCity NuGet feed<bs:help file="using-teamcity-as-nuget-feed"/>, specify the URL from the NuGet feed project settings page.<br />
        For feeds with authentication, configure the <em>NuGet Feed Credentials</em> build feature
        <bs:help file="NuGet+Feed+Credentials"/>
  </td>
</tr>

<tr class="dotnet build clean msbuild pack publish restore run">
  <th class="noBorder"><label for="${params.runtimeKey}">Runtime:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.runtimeKey}" className="longField"/>
      <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                      targetFieldId="${params.runtimeKey}" popupTitle="Select runtime"
                      selectionMode="single"/>
    </div>
    <span class="error" id="error_${params.runtimeKey}"></span>
    <span class="smallNote">Specify the target runtime.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet pack publish run">
  <th class="noBorder">Options:</th>
  <td>
    <props:checkboxProperty name="${params.skipBuildKey}"/>
    <label for="${params.skipBuildKey}">Do not build the projects</label>
  </td>
</tr>

<tr class="advancedSetting dotnet test">
  <th class="noBorder">Options:</th>
  <td>
    <props:checkboxProperty name="${params.skipBuildKey}"/>
    <label for="${params.skipBuildKey}">Do not build the projects</label>
    <br/>
    <props:checkboxProperty name="${params.singleSessionKey}"/>
    <label for="${params.singleSessionKey}">Run tests in a single session</label>
    <span class="smallNote">Runs tests for assembly files in a single test session. A test session must include assemblies related to a single target framework.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet build clean pack publish test">
  <th class="noBorder"><label for="${params.outputDirKey}">Output directory:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.outputDirKey}" className="longField"/>
      <bs:vcsTree fieldId="${params.outputDirKey}" dirsOnly="true"/>
    </div>
    <span class="error" id="error_${params.outputDirKey}"></span>
    <span class="smallNote">The directory where to place outputs.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet build pack publish">
  <th class="noBorder"><label for="${params.versionSuffixKey}">Version suffix:</label></th>
  <td>
    <props:textProperty name="${params.versionSuffixKey}" className="longField" expandable="true"/>
    <span class="error" id="error_${params.versionSuffixKey}"></span>
    <span class="smallNote">Defines the value for the $(VersionSuffix) property in the project.</span>
  </td>
</tr>

<tr class="dotnet nuget-delete nuget-push">
  <th class="noBorder"><label for="${params.nugetPackageSourceKey}">NuGet Server: <l:star/></label></th>
  <td>
    <props:textProperty name="${params.nugetPackageSourceKey}" className="longField"/>
    <%--<bs:projectData type="NuGetFeedUrls" sourceFieldId="queryString" selectionMode="single"
                    targetFieldId="${params.nugetPackageSourceKey}" popupTitle="Select TeamCity NuGet feed"/>--%>
    <span class="error" id="error_${params.nugetPackageSourceKey}"></span>
    <span class="smallNote">
        Specify the NuGet feed URL.<br />
        To use a TeamCity NuGet feed<bs:help file="using-teamcity-as-nuget-feed"/>, specify the URL from the NuGet feed project settings page.<br />
        For feeds with authentication, configure the <em>NuGet Feed Credentials</em> build feature
        <bs:help file="NuGet+Feed+Credentials"/>
  </td>
</tr>

<tr class="dotnet nuget-delete">
  <th class="noBorder"><label for="${params.nugetPackageIdKey}">Package ID: <l:star/></label></th>
  <td>
    <props:textProperty name="${params.nugetPackageIdKey}" className="longField"/>
    <span class="error" id="error_${params.nugetPackageIdKey}"></span>
    <span class="smallNote">Specify the package name and version separated by a space.</span>
  </td>
</tr>

<tr class="dotnet nuget-delete nuget-push">
  <th class="noBorder"><label for="${params.nugetApiKey}">API key: <l:star/></label></th>
  <td>
    <props:passwordProperty name="${params.nugetApiKey}" className="longField"/>
    <span class="error" id="error_${params.nugetApiKey}"></span>
    <span class="smallNote">
      Specify the API key to access the NuGet packages feed.<br/>
      For the built-in TeamCity NuGet feeds<bs:help file="Using+TeamCity+as+NuGet+Feed"/>
      use the <em>%teamcity.nuget.feed.api.key%</em>.
    </span>
  </td>
</tr>

<tr class="advancedSetting dotnet restore">
  <th class="noBorder"><label for="${params.nugetPackagesDirKey}">Packages directory:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.nugetPackagesDirKey}" className="longField"/>
      <bs:vcsTree fieldId="${params.nugetPackagesDirKey}" dirsOnly="true"/>
    </div>
    <span class="error" id="error_${params.nugetPackagesDirKey}"></span>
    <span class="smallNote">Directory to install packages in.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet restore">
  <th class="noBorder"><label for="${params.nugetConfigFileKey}">Configuration file:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.nugetConfigFileKey}" className="longField"/>
      <bs:vcsTree fieldId="${params.nugetConfigFileKey}"/>
    </div>
    <span class="error" id="error_${params.nugetConfigFileKey}"></span>
    <span class="smallNote">The NuGet configuration file to use.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet nuget-push">
  <th class="noBorder">Options:</th>
  <td>
    <props:checkboxProperty name="${params.nugetNoSymbolsKey}"/>
    <label for="${params.nugetNoSymbolsKey}">Do not publish nuget symbol packages</label>
  </td>
</tr>

<tr class="advancedSetting dotnet test vstest">
  <th class="noBorder"><label for="${params.testSettingsFileKey}">Settings file:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.testSettingsFileKey}" className="longField"/>
      <bs:vcsTree fieldId="${params.testSettingsFileKey}"/>
    </div>
    <span class="error" id="error_${params.testSettingsFileKey}"></span>
    <span class="smallNote">The path to the run settings configuration file.</span>
  </td>
</tr>

<tr class="advancedSetting" id="logging">
  <th class="noBorder"><label for="${params.verbosityKey}">Logging verbosity:</label></th>
  <td>
    <props:selectProperty name="${params.verbosityKey}" enableFilter="true" className="mediumField">
      <props:option value="">&lt;Default&gt;</props:option>
      <c:forEach var="item" items="${params.verbosityValues}">
        <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${params.verbosityKey}"></span>
  </td>
</tr>

<tr id="commandLine" class="advancedSetting">
  <th><label for="${params.argumentsKey}">Command line parameters: <a id="dotnet-help" target="_blank" rel="noreferrer" showdiscardchangesmessage="false"><bs:helpIcon iconTitle=""/></a></label></th>
  <td>
    <props:textProperty name="${params.argumentsKey}" className="longField" expandable="true"/>
    <span class="error" id="error_${params.argumentsKey}"></span>
    <span class="smallNote">Enter additional command line parameters.</span>
  </td>
</tr>

</tbody>

<tbody id="dotnet-coverage" class="hidden">
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

<button id="queryString" style="display: none"></button>

<tbody>

<script type="text/javascript">
  BS.DotnetParametersForm.updateElements();
  $('queryString').value = encodeURIComponent(BS.DotnetParametersForm.getFeedUrlQueryString());
</script>