<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<tr class="advancedSetting">
    <th><label for="${params.buildFrameworkKey}">Framework:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.buildFrameworkKey}" className="longField"/>
            <bs:projectData type="DotnetFrameworks" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.buildFrameworkKey}" popupTitle="Select frameworks"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.buildFrameworkKey}"></span>
        <span class="smallNote">Compile a specific framework.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.buildConfigKey}">Configuration:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.buildConfigKey}" className="longField"/>
            <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.buildConfigKey}" popupTitle="Select configuration"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.buildConfigKey}"></span>
        <span class="smallNote">Configuration under which to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.buildRuntimeKey}">Runtime:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.buildRuntimeKey}" className="longField"/>
            <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.buildRuntimeKey}" popupTitle="Select runtime"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.buildRuntimeKey}"></span>
        <span class="smallNote">Target runtime to publish for.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th>Options:</th>
    <td>
        <props:checkboxProperty name="${params.buildProfileKey}"/>
        <label for="${params.buildProfileKey}">Print the incremental safety checks that prevent incremental compilation</label><br/>
        <props:checkboxProperty name="${params.buildNonIncrementalKey}"/>
        <label for="${params.buildNonIncrementalKey}">Turn off incremental build</label><br/>
        <props:checkboxProperty name="${params.buildNoDependenciesKey}"/>
        <label for="${params.buildNonIncrementalKey}">Ignore project to project references and only build the root project</label>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.buildOutputKey}">Output directory:</label></th>
    <td>
        <props:textProperty name="${params.buildOutputKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.buildOutputKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.buildOutputKey}"></span>
        <span class="smallNote">Directory in which to place outputs.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.buildTempKey}">Temp directory:</label></th>
    <td>
        <props:textProperty name="${params.buildTempKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.buildTempKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.buildTempKey}"></span>
        <span class="smallNote">Directory in which to place temporary outputs.</span>
    </td>
</tr>