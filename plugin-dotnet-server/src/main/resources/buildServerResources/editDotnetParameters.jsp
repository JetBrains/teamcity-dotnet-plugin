<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<c:set var="asterisk"><l:star/></c:set>

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
        if (element.name === "select") {
          element.selectedIndex = 0;
        } else {
          $element.val('').change();
        }
        $element.change();
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

        pathsRow.show()
      } else {
        pathsRow.hide();
        BS.DotnetParametersForm.clearInputValues(pathsRow);
      }

      $j("tr.dotnet").each(function(id, element) {
        var $row = $j(element);
        if (!$row.hasClass(commandName)) {
          $row.hide();
          BS.DotnetParametersForm.clearInputValues($row);
        } else {
          $row.show();
        }
      });
      $j(".runnerFormTable span.error").empty();

      var hideLogging = BS.DotnetParametersForm.hideLogging[commandName];
      $j(BS.Util.escapeId('logging')).toggleClass('hidden', !!hideLogging);

      var coverageEnabled = BS.DotnetParametersForm.coverageEnabled[commandName];
      var $coverageRow = $j(BS.Util.escapeId('dotnet-coverage'));
      $coverageRow.toggleClass('hidden', !coverageEnabled);
      if (!coverageEnabled) {
          BS.DotnetParametersForm.clearInputValues($coverageRow);
      }

      var helpUrl = 'https://docs.microsoft.com/en-us/dotnet/core/tools/dotnet' + (commandName !== '-' ? '-' + commandName : '');
      $j(BS.Util.escapeId('dotnet-help')).attr('href', helpUrl);

      var hideWorkingDirectory = BS.DotnetParametersForm.hideWorkingDirectory[commandName];
      var $workingDir = $j(BS.Util.escapeId('teamcity.build.workingDir'));
      $workingDir.closest('tr').toggleClass('hidden', !!hideWorkingDirectory);
      if (hideWorkingDirectory) {
          $workingDir.val('');
      }

      var init = BS.DotnetParametersForm.initFunctions[commandName];
      if (init) init();

      BS.MultilineProperties.updateVisible();
    }
  };

  $j(document).on('change', commandId, function () {
    BS.DotnetParametersForm.updateElements();
  });

  $j(document).on('ready', commandId, function () {
    BS.DotnetParametersForm.updateElements();
  });
</script>

<c:set var="commandTitle">Command:<bs:help urlPrefix="https://docs.microsoft.com/en-us/dotnet/core/tools/" file=""/></c:set>
<props:selectSectionProperty name="${params.commandKey}" title="${commandTitle}" note="">
  <c:forEach items="${params.commands}" var="type">
    <props:selectSectionPropertyContent value="${type.name}" caption="${type.description}">
      <jsp:include page="${teamcityPluginResourcesPath}/dotnet/${type.editPage}"/>
    </props:selectSectionPropertyContent>
  </c:forEach>
</props:selectSectionProperty>

<tr id="${params.pathsKey}-row" class="build clean">
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

<c:if test="${params.experimentalMode == true}">
  <tr class="advancedSetting dotnet msbuild">
    <th><label for="${params.msbuildVersionKey}">MSBuild version:</label></th>
    <td>
      <props:selectProperty name="${params.msbuildVersionKey}" enableFilter="true" className="mediumField">
        <props:option value="">&lt;Default&gt;</props:option>
        <c:forEach var="item" items="${params.msbuildVersions}">
          <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
        </c:forEach>
      </props:selectProperty>
      <span class="error" id="error_${params.msbuildVersionKey}"></span>
    </td>
  </tr>

  <tr class="advancedSetting dotnet vstest">
    <th><label for="${params.vstestVersionKey}">VSTest version:</label></th>
    <td>
      <props:selectProperty name="${params.vstestVersionKey}" enableFilter="true" className="mediumField">
        <props:option value="">&lt;Default&gt;</props:option>
        <c:forEach var="item" items="${params.vstestVersions}">
          <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
        </c:forEach>
      </props:selectProperty>
      <span class="error" id="error_${params.vstestVersionKey}"></span>
    </td>
  </tr>
</c:if>

<tr class="advancedSetting dotnet vstest">
  <th><label for="${params.testFilterKey}">Tests filtration:</label></th>
  <td>
    <props:selectProperty name="${params.testFilterKey}" enableFilter="true" className="mediumField">
      <props:option value="">&lt;Disabled&gt;</props:option>
      <props:option value="name">Test names</props:option>
      <props:option value="filter">Test case filter</props:option>
    </props:selectProperty>
  </td>
</tr>

<c:set var="testNamesHint">
  Run tests with names that match the provided values.
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
  <th><label for="${params.frameworkKey}">Framework:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.frameworkKey}" className="longField"/>
      <bs:projectData type="DotnetFrameworks" sourceFieldId="${params.pathsKey}"
                      targetFieldId="${params.frameworkKey}" popupTitle="Select frameworks"
                      selectionMode="single"/>
    </div>
    <span class="error" id="error_${params.frameworkKey}"></span>
    <span class="smallNote">Specify the target framework.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet msbuild">
  <th><label for="${params.targetsKey}">Targets:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.targetsKey}" className="longField"/>
      <bs:projectData type="DotnetTargets" sourceFieldId="${params.pathsKey}"
                      targetFieldId="${params.targetsKey}" popupTitle="Select targets"/>
    </div
    <span class="error" id="error_${params.targetsKey}"></span>
    <span class="smallNote">Enter the list of build targets.</span>
  </td>
</tr>

<tr class="dotnet devenv">
  <th><label for="${params.visualStudioActionKey}">Build Action: <l:star/></label></th>
  <td>
    <props:selectProperty name="${params.visualStudioActionKey}" enableFilter="true" className="mediumField">
      <props:option value="">&lt;Select&gt;</props:option>
      <c:forEach var="item" items="${params.visualStudioActions}">
        <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${params.visualStudioActionKey}"></span>
  </td>
</tr>

<tr class="advancedSetting dotnet devenv">
  <th><label for="${params.visualStudioVersionKey}">Visual Studio Version:</label></th>
  <td>
    <props:selectProperty name="${params.visualStudioVersionKey}" enableFilter="true" className="mediumField">
      <props:option value="">&lt;Default&gt;</props:option>
      <c:forEach var="item" items="${params.visualStudioVersions}">
        <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${params.visualStudioVersionKey}"></span>
  </td>
</tr>

<tr class="advancedSetting dotnet build clean msbuild pack publish run test devenv">
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

<tr class="advancedSetting dotnet devenv">
  <th class="noBorder"><label for="${params.platformKey}">Platform:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.platformKey}" className="longField"/>
      <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                      targetFieldId="${params.platformKey}" popupTitle="Select platform"
                      selectionMode="single"/>
    </div>
    <span class="error" id="error_${params.platformKey}"></span>
    <span class="smallNote">Specify the target platform.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet restore">
  <th><label for="${params.nugetPackageSourcesKey}">NuGet package sources:</label></th>
  <td>
    <c:set var="note">
      Specifies NuGet package sources to use during the restore.<br/>
      To use a TeamCity NuGet feed<bs:help file="Using+TeamCity+as+NuGet+Server"/>, specify the URL from the NuGet feed project settings page.
    </c:set>
    <props:multilineProperty name="${params.nugetPackageSourcesKey}" className="longField" expanded="true"
                             cols="60" rows="3" linkTitle="Sources" note="${note}"/>
    <bs:projectData type="NuGetFeedUrls" sourceFieldId="buildTypeId"
                    targetFieldId="${params.nugetPackageSourcesKey}" popupTitle="Select TeamCity NuGet feeds"/>
    <span class="error" id="error_${params.nugetPackageSourcesKey}"></span>
  </td>
</tr>

<tr class="advancedSetting dotnet build clean msbuild pack publish restore run">
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

<tr class="advancedSetting dotnet build clean pack publish test">
  <th><label for="${params.outputDirKey}">Output directory:</label></th>
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
  <th><label for="${params.versionSuffixKey}">Version suffix:</label></th>
  <td>
    <props:textProperty name="${params.versionSuffixKey}" className="longField" expandable="true"/>
    <span class="error" id="error_${params.versionSuffixKey}"></span>
    <span class="smallNote">Defines the value for the $(VersionSuffix) property in the project.</span>
  </td>
</tr>

<tr class="dotnet nuget-delete nuget-push">
  <th><label for="${params.nugetPackageSourceKey}">NuGet Server: <l:star/></label></th>
  <td>
    <props:textProperty name="${params.nugetPackageSourceKey}" className="longField"/>
    <bs:projectData type="NuGetFeedUrls" sourceFieldId="buildTypeId" selectionMode="single"
                    targetFieldId="${params.nugetPackageSourceKey}" popupTitle="Select TeamCity NuGet feed"/>
    <span class="error" id="error_${params.nugetPackageSourceKey}"></span>
    <span class="smallNote">
            Specify the server URL. To use a TeamCity NuGet feed<bs:help file="Using+TeamCity+as+NuGet+Server"/>, specify the URL from the
            NuGet feed project settings page.
        </span>
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
            For the built-in TeamCity NuGet feeds use the <em>%teamcity.nuget.feed.api.key%</em>.
        </span>
  </td>
</tr>

<tr class="advancedSetting dotnet restore">
  <th><label for="${params.nugetPackagesDirKey}">Packages directory:</label></th>
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
  <th><label for="${params.nugetConfigFileKey}">Configuration file:</label></th>
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
  <th>Options:</th>
  <td>
    <props:checkboxProperty name="${params.nugetNoSymbolsKey}"/>
    <label for="${params.nugetNoSymbolsKey}">Do not publish an existing nuget symbols packages</label>
  </td>
</tr>

<tr class="advancedSetting dotnet test vstest">
  <th><label for="${params.testSettingsFileKey}">Settings file:</label></th>
  <td>
    <div class="position-relative">
      <props:textProperty name="${params.testSettingsFileKey}" className="longField"/>
      <bs:vcsTree fieldId="${params.testSettingsFileKey}"/>
    </div>
    <span class="error" id="error_${params.testSettingsFileKey}"></span>
    <span class="smallNote">The path to the run settings configuration file.</span>
  </td>
</tr>

<tr class="advancedSetting dotnet pack test">
  <th>Options:</th>
  <td>
    <props:checkboxProperty name="${params.skipBuildKey}"/>
    <label for="${params.skipBuildKey}">Do not build the projects</label>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${params.argumentsKey}">Command line parameters:</label></th>
  <td>
    <props:textProperty name="${params.argumentsKey}" className="longField" expandable="true"/>
    <span class="error" id="error_${params.argumentsKey}"></span>
    <span class="smallNote">
            Enter additional command line parameters for dotnet. <a
        id="dotnet-help" target="_blank" showdiscardchangesmessage="false"><bs:helpIcon iconTitle=""/></a>
        </span>
  </td>
</tr>

<tr class="advancedSetting" id="logging">
  <th><label for="${params.verbosityKey}">Logging verbosity:</label></th>
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

<button id="buildTypeId" style="display: none"></button>

<tbody>

<c:if test="${params.experimentalMode == true}">
<tr class="advancedSetting">
  <th><label for="${params.integrationPackagePathKey}">Integration package: </label></th>
  <td>
    <jsp:include page="/tools/selector.html?toolType=${params.integrationPackageToolTypeKey}&versionParameterName=${params.integrationPackagePathKey}&class=${clazz}"/>
  </td>
</tr>
</c:if>

<script type="text/javascript">
  BS.DotnetParametersForm.updateElements();
  $('buildTypeId').value = window.location.search.substring(1).split('&').grep(/id=buildType:(.*)/).join('').split(':')[1] + ":guestAuth";
</script>