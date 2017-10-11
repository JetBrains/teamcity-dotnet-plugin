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