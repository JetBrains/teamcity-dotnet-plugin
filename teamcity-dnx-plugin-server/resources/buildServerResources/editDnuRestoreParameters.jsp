<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<tr class="advancedSetting">
    <th><label for="${params.restorePathsKey}">Projects:</label></th>
    <td>
        <props:textProperty name="${params.restorePathsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.restorePathsKey}"></span>
        <span class="smallNote">Newline-separated list of projects and project folders to restore.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.packagePathsKey}">Packages path:</label></th>
    <td>
        <props:textProperty name="${params.packagePathsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.packagePathsKey}"></span>
        <span class="smallNote">Path to restore packages.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"></th>
    <td class="noBorder">
        <props:checkboxProperty name="${params.parallelExecutionKey}"/>
        <label for="${params.parallelExecutionKey}">Parallel execution for multiple discovered projects</label>
    </td>
</tr>