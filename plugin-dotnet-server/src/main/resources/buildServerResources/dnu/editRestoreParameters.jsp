<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DnuParametersProvider"/>

<tr class="advancedSetting">
    <th><label for="${params.restorePackagesKey}">Packages path:</label></th>
    <td>
        <props:textProperty name="${params.restorePackagesKey}" className="longField"/>
        <bs:vcsTree fieldId="${params.restorePackagesKey}" dirsOnly="true"/>
        <span class="error" id="error_${params.restorePackagesKey}"></span>
        <span class="smallNote">Path to restore packages.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"></th>
    <td class="noBorder">
        <props:checkboxProperty name="${params.restoreParallelKey}"/>
        <label for="${params.restoreParallelKey}">Parallel execution for multiple discovered projects</label>
    </td>
</tr>