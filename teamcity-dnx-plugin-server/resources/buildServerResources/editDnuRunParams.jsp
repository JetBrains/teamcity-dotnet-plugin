<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>

<tr>
    <th><label for="${params.commandKey}">Command:</label></th>
    <td>
        <props:selectProperty name="${params.commandKey}" enableFilter="true" className="mediumField">
            <c:forEach var="item" items="${params.commands}">
                <props:option value="${item}"><c:out value="${item}"/></props:option>
            </c:forEach>
        </props:selectProperty>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.projectPathsKey}">Projects:</label></th>
    <td>
        <props:multilineProperty
                name="${params.projectPathsKey}"
                linkTitle="Specify DNX project paths"
                rows="3"
                cols="60"
                expanded="${true}" className="longField"
                note="Newline-separated list of projects and project folders to restore."/>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.packagePathsKey}">Packages path:</label></th>
    <td class="noBorder">
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

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.argumentsKey}">Command line parameters:</label></th>
    <td class="noBorder">
        <props:textProperty name="${params.argumentsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.argumentsKey}"></span>
        <span class="smallNote">Enter additional command line parameters to DNX Utility.</span>
    </td>
</tr>
