<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
  BS.dotCover = {
    updateDotCoverElements: function(prefix) {
      if ($j('.' + prefix + '_dotCover').prop('checked')) {
        $j('#' + prefix + '_dotCoverHeader').prop('rowSpan', "5")
        $j('#' + prefix + '_dotCoverToolType').removeClass('hidden');
        $j('#' + prefix + '_dotCoverFilters').removeClass('hidden');
        $j('#' + prefix + '_dotCoverAttributeFilters').removeClass('hidden');
        $j('#' + prefix + '_dotCoverArguments').removeClass('hidden');
      }
      else {
        $j('#' + prefix + '_dotCoverToolType').addClass('hidden');
        $j('#' + prefix + '_dotCoverFilters').addClass('hidden');
        $j('#' + prefix + '_dotCoverAttributeFilters').addClass('hidden');
        $j('#' + prefix + '_dotCoverArguments').addClass('hidden');
        $j('#' + prefix + '_dotCoverHeader').prop('rowSpan', "1")
      }

      BS.MultilineProperties.updateVisible();
    }
  }
</script>

<tr class="advancedSetting">
    <th id="${param.prefix}_dotCoverHeader"><label for="${params.dotCoverToolType}">Code coverage:</label></th>
    <td><props:checkboxProperty className="${param.prefix}_dotCover" name="${params.dotCoverEnabled}" onclick="BS.dotCover.updateDotCoverElements('${param.prefix}');"/></td>
</tr>

<tr class="advancedSetting hidden" id="${param.prefix}_dotCoverToolType">
    <td>
        <jsp:include page="/tools/selector.html?toolType=${params.dotCoverToolType}&versionParameterName=${params.dotCoverHome}&class=longField"/>
    </td>
</tr>

<tr class="advancedSetting hidden" id="${param.prefix}_dotCoverFilters">
    <label for="${params.dotCoverFilters}">Filters:</label>
    <td>
        <c:set var="note">
            Specify a new-line separated list of filters for code coverage. Use the <i>+:myassemblyName</i> or <i>-:myassemblyName</i> syntax to
            include or exclude an assembly (by name, without extension) from code coverage. Use asterisk (*) as a wildcard if needed.<bs:help file="JetBrains+dotCover"/>
        </c:set>
        <props:multilineProperty name="${params.dotCoverFilters}" className="longField" expanded="true" cols="60" rows="4" linkTitle="Assemblies Filters" note="${note}"/>
    </td>
</tr>

<tr class="advancedSetting hidden" id="${param.prefix}_dotCoverAttributeFilters">
    <label for="${cns.dotCoverAttributeFilters}">Attribute Filters:</label>
    <td>
        <c:set var="note">
            Specify a new-line separated list of attribute filters for code coverage. Use the <i>-:attributeName</i> syntax to exclude a code marked with attributes from code coverage. Use asterisk (*) as a wildcard if needed.<bs:help file="JetBrains+dotCover"/>
        </c:set>
        <props:multilineProperty name="${params.dotCoverAttributeFilters}" className="longField" cols="60" rows="4" linkTitle="Attribute Filters" note="${note}"/>
        <span class="smallNote"><strong>Supported only with dotCover 2.0 or newer</strong></span>
    </td>
</tr>

<tr class="advancedSetting hidden" id="${param.prefix}_dotCoverArguments">
    <label for="${params.dotCoverArguments}">Additional dotCover.exe arguments:</label>
    <td>
        <props:multilineProperty name="${params.dotCoverArguments}" linkTitle="Edit command line" cols="60" rows="5" />
        <span class="smallNote">Additional commandline parameters to add to calling dotCover.exe separated by new lines.</span>
        <span id="error_${params.dotCoverArguments}" class="error"></span>
    </td>
</tr>

<script type="text/javascript">
  BS.dotCover.updateDotCoverElements('${param.prefix}');
</script>