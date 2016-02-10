<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.pathsKey}">Project:</label></th>
    <td>
        <div class="completionIconWrapper clearfix">
            <div class="dnx left">
                <props:textProperty name="${params.pathsKey}" id="${params.pathsKey}-build" className="longField"
                                    expandable="true"/>
            </div>
            <bs:vcsTree fieldId="${params.pathsKey}-build"/>
        </div>
        <span class="error" id="error_${params.pathsKey}-build"></span>
        <span class="smallNote">The project path to compile, defaults to the current directory.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.buildFrameworkKey}">Framework:</label></th>
    <td>
        <div class="completionIconWrapper">
            <props:textProperty name="${params.buildFrameworkKey}" className="longField"/>
            <bs:projectData type="DnxFrameworks" sourceFieldId="${params.pathsKey}-build"
                            targetFieldId="${params.buildFrameworkKey}" popupTitle="Select frameworks"/>
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
            <bs:projectData type="DnxConfigurations" sourceFieldId="${params.pathsKey}-build"
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
            <bs:projectData type="DnxRuntimes" sourceFieldId="${params.pathsKey}-build"
                            targetFieldId="${params.buildRuntimeKey}" popupTitle="Select runtime"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.buildRuntimeKey}"></span>
        <span class="smallNote">Target runtime to publish for.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.buildArchKey}">Architecture:</label></th>
    <td>
        <props:selectProperty name="${params.buildArchKey}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Default&gt;</props:option>
            <props:option value="x64">x64</props:option>
        </props:selectProperty>
        <span class="error" id="error_${params.buildArchKey}"></span>
        <span class="smallNote">The architecture for which to compile.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th>Compilation</th>
    <td>
        <props:checkboxProperty name="${params.buildNativeKey}"
                                onclick="$('${params.buildCppKey}').disabled = this.checked ? '' : 'disabled'; BS.VisibilityHandlers.updateVisibility('mainContent');"/>
        <label for="${params.buildNativeKey}">Compiles source to native machine code</label><br/>
        <props:checkboxProperty name="${params.buildCppKey}"
                                disabled="${empty propertiesBean.properties[params.buildNativeKey]}"/>
        <label for="${params.buildCppKey}">Make native compilation with C++ code generator</label><br/>
        <props:checkboxProperty name="${params.buildProfileKey}"/>
        <label for="${params.buildProfileKey}">Print the incremental safety checks to prevent incremental
            compilation</label><br/>
        <props:checkboxProperty name="${params.buildNonIncrementalKey}"/>
        <label for="${params.buildNonIncrementalKey}">Mark the entire build as not safe for incrementality</label>
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