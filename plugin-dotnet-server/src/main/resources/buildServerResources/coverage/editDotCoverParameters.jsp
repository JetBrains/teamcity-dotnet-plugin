<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<tr class="advancedSetting">
    <th>dotCover CLT Home Directory:</th>
    <td>
        <jsp:include page="/tools/selector.html?toolType=JetBrains.dotCover.CommandLineTools&versionParameterName=${params.dotCoverHomeKey}&class=longField"/>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.dotCoverFiltersKey}">Filters:</label></th>
    <td>
        <c:set var="note">
            Specify a new-line separated list of filters for code coverage. Use the <i>+:myassemblyName</i> or <i>-:myassemblyName</i> syntax to
            include or exclude an assembly (by name, without extension) from code coverage. Use the asterisk (*) as a wildcard if needed.<bs:help file="JetBrains+dotCover"/>
        </c:set>
        <props:multilineProperty name="${params.dotCoverFiltersKey}" className="longField" expanded="true" cols="60" rows="4" linkTitle="Assemblies filters" note="${note}"/>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.dotCoverAttributeFiltersKey}">Attribute filters:</label></th>
    <td>
        <c:set var="note">
            Specify a new-line separated list of attribute filters for code coverage. Use the <i>-:attributeName</i> syntax to exclude the code marked with attributes from code coverage. Use the asterisk (*) as a wildcard if needed.<bs:help file="JetBrains+dotCover"/>
        </c:set>
        <props:multilineProperty name="${params.dotCoverAttributeFiltersKey}" className="longField" cols="60" rows="4" linkTitle="Attribute filters" note="${note}"/>
        <span class="smallNote"><strong>Supported only with dotCover 2.0 or later</strong></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.dotCoverArgumentsKey}">Additional arguments:</label></th>
    <td>
        <props:multilineProperty name="${params.dotCoverArgumentsKey}" linkTitle="Edit command line" cols="60" rows="5" />
        <span class="smallNote">Enter additional new-line sepatated command line parameters for dotCover.</span>
        <span id="error_${params.dotCoverArgumentsKey}" class="error"></span>
    </td>
</tr>