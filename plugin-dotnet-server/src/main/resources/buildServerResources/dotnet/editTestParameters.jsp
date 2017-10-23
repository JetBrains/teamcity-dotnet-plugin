<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
  BS.DotnetParametersForm.appendProjectFile.push("test");
  BS.DotnetParametersForm.paths["test"] = "Projects";
  BS.DotnetParametersForm.coverageEnabled["test"] = true;
</script>

<tr class="advancedSetting">
    <th>
        <label for="${params.testTestCaseFilterKey}">Test case filter:</label>
    </th>
    <td>
        <props:textProperty name="${params.testTestCaseFilterKey}" className="longField" />
        <span class="error" id="error_${params.testTestCaseFilterKey}"></span>
        <span class="smallNote">Run tests that match the given expression.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.testFrameworkKey}">Framework:</label></th>
    <td>
        <div class="position-relative">
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
        <div class="position-relative">
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
    <th><label for="${params.testOutputKey}">Output directory:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.testOutputKey}" className="longField"/>
            <bs:vcsTree fieldId="${params.testOutputKey}" dirsOnly="true"/>
        </div>
        <span class="error" id="error_${params.testOutputKey}"></span>
        <span class="smallNote">Directory where to find the binaries to be run.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th>Options:</th>
    <td>
        <props:checkboxProperty name="${params.testNoBuildKey}"/>
        <label for="${params.testNoBuildKey}">Do not build the project before testing</label>
    </td>
</tr>

<tr class="advancedSetting">
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