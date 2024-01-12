<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>


<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<tr class="advancedSetting">
  <th>dotCover CLT home directory:</th>
  <td>
    <jsp:include page="/tools/selector.html?toolType=JetBrains.dotCover.CommandLineTools&versionParameterName=${params.dotCoverHomeKey}&class=longField"/>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${params.dotCoverFiltersKey}">Assembly filters:</label></th>
  <td>
    <c:set var="note">
      Type "<i>+:assemblyName</i>" to include or "<i>-:assemblyName</i>" to exclude assemblies to/from the code coverage.
      Each rule should start from a new line. Use asterisk (*) as a wildcard for any string.<bs:help file="JetBrains+dotCover"/>
    </c:set>
    <props:multilineProperty name="${params.dotCoverFiltersKey}" className="longField" expanded="true" cols="60" rows="4" linkTitle="Assembly filters" note="${note}"/>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${params.dotCoverAttributeFiltersKey}">Attribute filters:</label></th>
  <td>
    <c:set var="note">
      Type "<i>-:attributeName</i>" to exclude any code marked with this attribute from the code coverage.
      Each rule should start from a new line. Use asterisk (*) as a wildcard for any string. <bs:help file="JetBrains+dotCover"/>
    </c:set>
    <props:multilineProperty name="${params.dotCoverAttributeFiltersKey}" className="longField" cols="60" rows="4" linkTitle="Attribute filters" note="${note}"/>
    <span class="smallNote"><strong>Applicable to dotCover 2.0 or higher.</strong></span>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${params.dotCoverArgumentsKey}">Additional arguments:</label></th>
  <td>
    <props:multilineProperty name="${params.dotCoverArgumentsKey}" linkTitle="Additional arguments" cols="60" rows="5"/>
    <span class="smallNote">Additional new-line separated command line parameters for dotCover.</span>
    <span id="error_${params.dotCoverArgumentsKey}" class="error"></span>
  </td>
</tr>