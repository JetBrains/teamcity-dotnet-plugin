<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>
<c:set var="filterBlockId" value="${util:forJSIdentifier(params.vstestFilterTypeKey)}_Block_js"/>

<script type="text/javascript">
    BS.DotnetParametersForm.paths["vstest"] = "Test assemblies";
    BS.DotnetParametersForm.mandatoryPaths["vstest"] = true;
    BS.DotnetParametersForm.coverageEnabled["vstest"] = true;
    BS.DotnetParametersForm.initFunctions["vstest"] = function () {
        BS.SelectSectionProperty_${filterBlockId}.onRendered();
    };
</script>

<c:if test="${params.experimentalMode == true}">
    <tr class="advancedSetting">
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

<props:selectSectionProperty name="${params.vstestFilterTypeKey}" title="Tests filtration:">
    <props:selectSectionPropertyContent value="" caption="<Disabled>">
    </props:selectSectionPropertyContent>

    <props:selectSectionPropertyContent value="name" caption="Test names">
        <tr class="advancedSetting">
            <th class="noBorder">
                <label for="${params.vstestTestNamesKey}">Test names: <l:star/></label>
            </th>
            <td>
                <props:multilineProperty expanded="true" name="${params.vstestTestNamesKey}" className="longField"
                                         rows="3" cols="49" linkTitle="Edit test names"
                                         note="Run tests with names that match the provided values." />
            </td>
        </tr>
    </props:selectSectionPropertyContent>

    <props:selectSectionPropertyContent value="filter" caption="Test case filter">
        <tr class="advancedSetting">
            <th class="noBorder">
                <label for="${params.vstestTestCaseFilterKey}">Test case filter: <l:star/></label>
            </th>
            <td>
                <props:textProperty name="${params.vstestTestCaseFilterKey}" className="longField" />
                <span class="error" id="error_${params.vstestTestCaseFilterKey}"></span>
                <span class="smallNote">Run tests that match the given expression.</span>
            </td>
        </tr>
    </props:selectSectionPropertyContent>
</props:selectSectionProperty>

<c:if test="${params.experimentalMode == true or
    not empty propertiesBean.properties[params.vstestPlatformKey]}">
    <tr class="advancedSetting">
        <th><label for="${params.vstestPlatformKey}">Target platform:</label></th>
        <td>
            <props:textProperty name="${params.vstestPlatformKey}" className="longField"/>
            <span class="error" id="error_${params.vstestPlatformKey}"></span>
            <span class="smallNote">The target platform architecture to be used for test execution</span>
        </td>
    </tr>
</c:if>

<tr class="advancedSetting">
    <th><label for="${params.vstestFrameworkKey}">Framework:</label></th>
    <td>
        <props:textProperty name="${params.vstestFrameworkKey}" className="longField" />
        <span class="error" id="error_${params.vstestFrameworkKey}"></span>
        <span class="smallNote">The target .NET Framework version to be used for test execution</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.vstestSettingsFileKey}">Settings file:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.vstestSettingsFileKey}" className="longField"/>
            <bs:vcsTree fieldId="${params.vstestSettingsFileKey}"/>
        </div>

        <span class="error" id="error_${params.vstestSettingsFileKey}"></span>
        <span class="smallNote">The path to the run settings configuration file</span>
    </td>
</tr>

<c:if test="${params.experimentalMode == true or
    not empty propertiesBean.properties[params.vstestInIsolationKey]}">
    <tr class="advancedSetting">
        <th><label for="${params.vstestInIsolationKey}">Run in isolation:</label></th>
        <td>
            <props:checkboxProperty name="${params.vstestInIsolationKey}"/>
            <label for="${params.vstestInIsolationKey}">Runs the tests in an isolated process</label>
            <span class="error" id="error_${params.vstestInIsolationKey}"></span>
        </td>
    </tr>
</c:if>