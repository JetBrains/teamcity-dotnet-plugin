<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.paths["test"] = "Projects";
    debugger;
    BS.dotnet = {
      updateDotCoverElements: function() {
        if (document.getElementById('${params.dotCoverEnabled}').checked) {
          document.getElementById('dotCoverHeader').rowSpan = 5;
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
          document.getElementById('dotCoverHeader').rowSpan = 1;
        }
        BS.MultilineProperties.updateVisible();
      }
    }

</script>

<tr class="advancedSetting">
    <th><label for="${params.testFrameworkKey}">Framework:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.testFrameworkKey}" className="longField"/>
            <bs:projectData type="DotnetFrameworks" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.testFrameworkKey}" popupTitle="Select frameworks"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.testFrameworkKey}"></span>
        <span class="smallNote">Target framework to test for.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.testConfigKey}">Configuration:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.testConfigKey}" className="longField"/>
            <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.testConfigKey}" popupTitle="Select configurations"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.testConfigKey}"></span>
        <span class="smallNote">Configuration under which to test.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.testRuntimeKey}">Runtime:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.testRuntimeKey}" className="longField"/>
            <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.testRuntimeKey}" popupTitle="Select runtime"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.testRuntimeKey}"></span>
        <span class="smallNote">Target runtime to test for.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.testOutputKey}">Output directory:</label></th>
    <td>
        <props:textProperty name="${params.testOutputKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.testOutputKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.testOutputKey}"></span>
        <span class="smallNote">Directory in which to find the binaries to be run.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.testTempKey}">Temp directory:</label></th>
    <td>
        <props:textProperty name="${params.testTempKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.testTempKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.testTempKey}"></span>
        <span class="smallNote">Directory in which to find temporary outputs.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th>Options:</th>
    <td>
        <props:checkboxProperty name="${params.testNoBuildKey}"/>
        <label for="${params.testNoBuildKey}">Do not build project before testing</label>
    </td>
</tr>

<tr class="advancedSetting">
    <th id="dotCoverHeader"><label for="${params.dotCoverToolType}">Code coverage:</label></th>
    <td><props:checkboxProperty name="${params.dotCoverEnabled}" onclick="BS.dotnet.updateDotCoverElements();"/></td>
</tr>

<tr class="advancedSetting hidden" id="dotCoverToolType">
    <td>
        <jsp:include page="/tools/selector.html?toolType=${params.dotCoverToolType}&versionParameterName=${params.dotCoverHome}&class=longField"/>
    </td>
</tr>

<tr class="advancedSetting hidden" id="dotCoverFilters">
    <label for="${params.dotCoverFilters}">Filters:</label>
    <td>
        <c:set var="note">
            Specify a new-line separated list of filters for code coverage. Use the <i>+:myassemblyName</i> or <i>-:myassemblyName</i> syntax to
            include or exclude an assembly (by name, without extension) from code coverage. Use asterisk (*) as a wildcard if needed.<bs:help file="JetBrains+dotCover"/>
        </c:set>
        <props:multilineProperty name="${params.dotCoverFilters}" className="longField" expanded="true" cols="60" rows="4" linkTitle="Assemblies Filters" note="${note}"/>
    </td>
</tr>

<tr class="advancedSetting hidden" id="dotCoverAttributeFilters">
    <label for="${cns.dotCoverAttributeFilters}">Attribute Filters:</label>
    <td>
        <c:set var="note">
            Specify a new-line separated list of attribute filters for code coverage. Use the <i>-:attributeName</i> syntax to exclude a code marked with attributes from code coverage. Use asterisk (*) as a wildcard if needed.<bs:help file="JetBrains+dotCover"/>
        </c:set>
        <props:multilineProperty name="${params.dotCoverAttributeFilters}" className="longField" cols="60" rows="4" linkTitle="Attribute Filters" note="${note}"/>
        <span class="smallNote"><strong>Supported only with dotCover 2.0 or newer</strong></span>
    </td>
</tr>

<tr class="advancedSetting hidden" id="dotCoverArguments">
    <label for="${params.dotCoverArguments}">Additional dotCover.exe arguments:</label>
    <td>
        <props:multilineProperty name="${params.dotCoverArguments}" linkTitle="Edit command line" cols="60" rows="5" />
        <span class="smallNote">Additional commandline parameters to add to calling dotCover.exe separated by new lines.</span>
        <span id="error_${params.dotCoverArguments}" class="error"></span>
    </td>
</tr>

<script type="text/javascript">
  BS.dotnet.updateDotCoverElements();
</script>