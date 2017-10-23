<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.paths["vstest"] = "Test assemblies";
    BS.DotnetParametersForm.coverageEnabled["vstest"] = true;

    var testFilterSelector = 'input[type=radio][name="prop:${params.vstestFilterTypeKey}"]';
    function updateElements(value) {
        $j(BS.Util.escapeId('${params.vstestTestNamesKey}')).prop('disabled', value !== 'name');
        $j(BS.Util.escapeId('${params.vstestTestCaseFilterKey}')).prop('disabled', value !== 'filter');
    }
    BS.DotnetParametersForm.initFunctions["vstest"] = function () {
        var $testFilterSelector = $j(testFilterSelector);
        var value = $testFilterSelector.filter(':checked').val() || 'name';

        $testFilterSelector.filter('input[value=' + value + ']').prop('checked', true);
        updateElements(value)
    };
    $j(document).on('change', testFilterSelector, function () {
        updateElements(this.value);
    });
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

<tr class="advancedSetting">
    <th>
        <props:radioButtonProperty name="${params.vstestFilterTypeKey}" value="name"
                                   checked="${propertiesBean.properties[params.vstestFilterTypeKey] == 'name' or
                                   not empty propertiesBean.properties[params.vstestTestNamesKey]}"/>
        <label for="${params.vstestTestNamesKey}">Test names:</label>
    </th>
    <td>
        <props:multilineProperty expanded="true" name="${params.vstestTestNamesKey}" className="longField"
                                 rows="3" cols="49" linkTitle="Edit test names"/>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder">
        <props:radioButtonProperty name="${params.vstestFilterTypeKey}" value="filter"
                                   checked="${propertiesBean.properties[params.vstestFilterTypeKey] == 'filter' or
                                   not empty propertiesBean.properties[params.vstestTestCaseFilterKey]}"/>
        <label for="${params.vstestTestCaseFilterKey}">Test case filter:</label>
    </th>
    <td>
        <props:textProperty name="${params.vstestTestCaseFilterKey}" className="longField" />
        <span class="error" id="error_${params.vstestTestCaseFilterKey}"></span>
        <span class="smallNote">Run tests that match the given expression.</span>
    </td>
</tr>

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