<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.paths["vstest"] = "Test file names";
    BS.DotnetParametersForm.dotCoverEnabled["vstest"] = true;
</script>

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

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.vstestConfigFileKey}">Run configuration file:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.vstestConfigFileKey}" className="longField"/>
        </div>
        <span class="error" id="error_${params.vstestConfigFileKey}"></span>
        <span class="smallNote">Path to run settings configuration file</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.vstestPlatformKey}">Target platform:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.vstestPlatformKey}" className="longField"/>
        </div>
        <span class="error" id="error_${params.vstestPlatformKey}"></span>
        <span class="smallNote">Target platform architecture to be used for test execution</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.vstestFrameworkKey}">Framework:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.vstestFrameworkKey}" className="longField" />
        </div>
        <span class="error" id="error_${params.vstestFrameworkKey}"></span>
        <span class="smallNote">Target .NET Framework version to be used for test execution</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.vstestTestNamesKey}">Test names:</label></th>
    <td>
        <div class="posRel">
            <props:multilineProperty expanded="true" name="${params.vstestTestNamesKey}" className="longField"
                                 note="Comma-separated list of test names."
                                 rows="3" cols="49" linkTitle="Edit test names"/>
        </div>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.vstestTestCaseFilterKey}">Test case filter:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.vstestTestCaseFilterKey}" className="longField" />
        </div>
        <span class="error" id="error_${params.vstestTestCaseFilterKey}"></span>
    </td>
</tr>


<tr class="advancedSetting">
    <th><label for="${params.vstestInIsolationKey}">Run in isolation:</label></th>
    <td>
        <div class="posRel">
            <props:checkboxProperty name="${params.vstestInIsolationKey}"/>
        </div>
        <label for="${inIsolation}">Runs the tests in an isolated process</label>
        <span class="error" id="error_${params.vstestInIsolationKey}"></span>
    </td>
</tr>
