<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<tr>
    <th><label for="${params.integrationPackagePathKey}">Integration package: </label></th>
    <td>
        <jsp:include page="/tools/selector.html?toolType=${params.integrationPackageToolTypeKey}&versionParameterName=${params.integrationPackagePathKey}&class=${clazz}"/>
    </td>
</tr>

<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}dotnet-settings.css'/>");

    BS.DotnetParametersForm = {
        appendProjectFile: [],
        dotCoverEnabled: [],
        hideLogging: [],
        targetsAreRequired: [],
        selectProjectFile: function (chosenFile) {
            var $paths = $j('#${params.pathsKey}');
            var value = BS.Util.trimSpaces($paths.val());
            var commandName = $j('#${params.commandKey}').val();
            var appendFile = BS.DotnetParametersForm.appendProjectFile.indexOf(commandName) >= 0;
            chosenFile = chosenFile.indexOf(" ") >= 0 ? '"' + chosenFile + '"' : chosenFile;
            $paths.val(appendFile && value.length > 0 ? value + " " + chosenFile : chosenFile);
        },
        paths: [],
        updateElements: function() {
          var commandName = $j('#${params.commandKey}').val();

          var hideLogging = BS.DotnetParametersForm.hideLogging[commandName];
          if (hideLogging == true) {
            $j('#logging').addClass('hidden');
          }
          else {
            $j('#logging').removeClass('hidden');
          }

          var targetsAreRequired = BS.DotnetParametersForm.targetsAreRequired[commandName];
          if (targetsAreRequired == true) {
            $j('#${params.pathsKey}-row').removeClass('advancedSetting');

          }
          else {
            $j('#${params.pathsKey}-row').addClass('advancedSetting');
          }

          BS.dotCover.showDotCoverSection();
        }
    };

    BS.dotCover = {
      showDotCoverElements: function() {
        if ($j('.dotCoverCheckBox').prop('checked')) {
          $j('#dotCoverHeader').prop('rowSpan', '5');
          $j('#dotCoverToolType').removeClass('hidden');
          $j('#dotCoverFilters').removeClass('hidden');
          $j('#dotCoverAttributeFilters').removeClass('hidden');
          $j('#dotCoverArguments').removeClass('hidden');
        }
        else {
          $j('#dotCoverToolType').addClass('hidden');
          $j('#dotCoverFilters').addClass('hidden');
          $j('#dotCoverAttributeFilters').addClass('hidden');
          $j('#dotCoverArguments').addClass('hidden');
          $j('#dotCoverHeader').prop('rowSpan', '1');
        }
      },

      updateDotCoverElements: function() {
        BS.dotCover.showDotCoverElements();
        BS.MultilineProperties.updateVisible();
      },

      showDotCoverSection: function() {
        var commandName = $j('#${params.commandKey}').val();
        var visible = BS.DotnetParametersForm.dotCoverEnabled[commandName];
        if (visible == true)
        {
          $j('#dotCoverCheckBox').removeClass('hidden');
        }
        else
        {
          $j('.dotCoverCheckBox').prop('checked', false);
          $j('#dotCoverCheckBox').addClass('hidden');
        }

        BS.dotCover.showDotCoverElements();
      }
    };

    $j(document).on('change', '#${params.commandKey}', function () {
        var command = $j(this).val();
        var pathsName = BS.DotnetParametersForm.paths[command];
        var pathsRow = $j("#${params.pathsKey}-row");
        if (pathsName) {
            pathsRow.show().find("label").text(pathsName + ':');
        } else {
            pathsRow.hide();
        }

        $j(".runnerFormTable span.error").empty();
        BS.DotnetParametersForm.updateElements();
    });

    $j(document).on('ready', '#${params.commandKey}', function () {
        $j(this).change();
    });
</script>

<props:selectSectionProperty name="${params.commandKey}" title="Command:" note="">
    <tr class="advancedSetting" id="${params.pathsKey}-row">
        <th class="noBorder"><label for="${params.pathsKey}">Projects:</label></th>
        <td>
            <props:textProperty name="${params.pathsKey}" className="longField" expandable="true">
                <jsp:attribute name="afterTextField"><bs:vcsTree treeId="${params.pathsKey}" callback="BS.DotnetParametersForm.selectProjectFile"/></jsp:attribute>
            </props:textProperty>
            <span class="error" id="error_${params.pathsKey}"></span>
            <span class="smallNote">Enter target files relative to the checkout directory separated by space or new line. Wildcards are supported.</span></td>
        </td>
    </tr>

    <props:workingDirectory/>

    <c:forEach items="${params.types}" var="type">
        <props:selectSectionPropertyContent value="${type.name}" caption="${type.name}">
            <jsp:include page="${teamcityPluginResourcesPath}/dotnet/${type.editPage}"/>
        </props:selectSectionPropertyContent>
    </c:forEach>
</props:selectSectionProperty>

<tr class="advancedSetting">
    <th><label for="${params.argumentsKey}">Command line parameters:</label></th>
    <td>
        <props:textProperty name="${params.argumentsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.argumentsKey}"></span>
        <span class="smallNote">Enter additional command line parameters to dotnet.</span>
    </td>
</tr>

<tr class="advancedSetting" id="logging">
    <th><label for="${params.verbosityKey}">Logging verbosity:</label></th>
    <td>
        <props:selectProperty name="${params.verbosityKey}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Default&gt;</props:option>
            <c:forEach var="item" items="${params.verbosity}">
                <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.verbosityKey}"></span>
    </td>
</tr>

<tr class="advancedSetting hidden" id="dotCoverCheckBox">
    <th id="dotCoverHeader"><label for="${params.dotCoverToolTypeKey}">Code coverage:</label></th>
    <td><props:checkboxProperty className="dotCoverCheckBox" name="${params.dotCoverEnabledKey}" onclick="BS.dotCover.updateDotCoverElements();"/></td>
</tr>

<tr class="advancedSetting hidden" id="dotCoverToolType">
    <td>
        <jsp:include page="/tools/selector.html?toolType=${params.dotCoverToolTypeKey}&versionParameterName=${params.dotCoverHomeKey}&class=longField"/>
    </td>
</tr>

<tr class="advancedSetting hidden" id="dotCoverFilters">
    <label for="${params.dotCoverFiltersKey}">Filters:</label>
    <td>
        <c:set var="note">
            Specify a new-line separated list of filters for code coverage. Use the <i>+:myassemblyName</i> or <i>-:myassemblyName</i> syntax to
            include or exclude an assembly (by name, without extension) from code coverage. Use asterisk (*) as a wildcard if needed.<bs:help file="JetBrains+dotCover"/>
        </c:set>
        <props:multilineProperty name="${params.dotCoverFiltersKey}" className="longField" expanded="true" cols="60" rows="4" linkTitle="Assemblies Filters" note="${note}"/>
    </td>
</tr>

<tr class="advancedSetting hidden" id="dotCoverAttributeFilters">
    <label for="${cns.dotCoverAttributeFilters}">Attribute Filters:</label>
    <td>
        <c:set var="note">
            Specify a new-line separated list of attribute filters for code coverage. Use the <i>-:attributeName</i> syntax to exclude a code marked with attributes from code coverage. Use asterisk (*) as a wildcard if needed.<bs:help file="JetBrains+dotCover"/>
        </c:set>
        <props:multilineProperty name="${params.dotCoverAttributeFiltersKey}" className="longField" cols="60" rows="4" linkTitle="Attribute Filters" note="${note}"/>
        <span class="smallNote"><strong>Supported only with dotCover 2.0 or newer</strong></span>
    </td>
</tr>

<tr class="advancedSetting hidden" id="dotCoverArguments">
    <label for="${params.dotCoverArgumentsKey}">Additional dotCover.exe arguments:</label>
    <td>
        <props:multilineProperty name="${params.dotCoverArgumentsKey}" linkTitle="Edit command line" cols="60" rows="5" />
        <span class="smallNote">Additional commandline parameters to add to calling dotCover.exe separated by new lines.</span>
        <span id="error_${params.dotCoverArgumentsKey}" class="error"></span>
    </td>
</tr>

<script type="text/javascript">
  BS.DotnetParametersForm.updateElements();
</script>