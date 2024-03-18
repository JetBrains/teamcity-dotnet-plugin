<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotCover.DotCoverRunnerParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<!-- It's necessary to make sure that dotCover home param won't be erased. See DotCoverParametersProcessor and TW-60495 -->
<props:hiddenProperty name="${params.coverageToolTypeKey}" value="${params.coverageToolTypeValue}" />

<tr>
  <th>dotCover tool:</th>
  <td>
    <jsp:include page="/tools/selector.html?toolType=JetBrains.dotCover.CommandLineTools&versionParameterName=${params.dotCoverHomeKey}&class=longField"/>
  </td>
</tr>

<tr>
  <th>Coverage settings:</th>
  <td>
    <props:multilineProperty name="${params.dotCoverCoveredProcessExecutableKey}" linkTitle="Executable (optional)" expanded="true" className="longField" cols="60" rows="1"/>
    <span class="smallNote">
      Specify path to an executable file to run the process under dotCover coverage profile and produce a dotCover snapshot file
    </span>
    <br/>
    <props:multilineProperty name="${params.dotCoverCoveredProcessArgumentsKey}" linkTitle="Command line arguments" className="longField" cols="60" rows="1"/>
    <span class="smallNote">Space or new-line separated command line parameters for covering process</span>
    <br/>
    <c:set var="generateReportActualValue" value='${propertiesBean.properties[params.dotCoverGenerateReportKey]}'/>
    <c:set var="generateReportDefaultValue" value="${propertiesBean.defaultProperties[params.dotCoverGenerateReportKey]}"/>
    <c:if test="${generateReportActualValue == generateReportDefaultValue}">
      <c:set var="generateReportChecked" value="${generateReportDefaultValue}"/>
    </c:if>
    <input type="checkbox" id="dotCoverGenerateReportCheckbox" ${generateReportChecked ? "checked=checked" : ""} />
    <props:hiddenProperty id="dotCoverGenerateReportHiddenInput" name="${params.dotCoverGenerateReportKey}" />
    <label for="dotCoverGenerateReportCheckbox">Generate coverage report</label>
    <span class="smallNote">Generates a TeamCity coverage report that will be displayed on the Code Coverage tab after the build is complete</span>
    <br/>
    <c:set var="additionalSnapshotsNote">
      <span>Specify dotCover snapshot (.dcvr) files paths separated by spaces or new lines.</span>
      <bs:helpLink file="Wildcards">Wildcards</bs:helpLink> are supported. Note that you can merge snapshots generated only by the selected or earlier version of dotCover tool
    </c:set>
    <props:multilineProperty
        name="${params.dotCoverAdditionalShapshotPathsKey}"
        className="longField" cols="60" rows="4"
        linkTitle="Include additional dotCover snapshots to the report"
        expanded="${not empty propertiesBean.properties[params.dotCoverAdditionalShapshotPathsKey]}"
        note="${additionalSnapshotsNote}"
    />
  </td>
</tr>

<tr class="advancedSetting">
  <th> </th>
  <td>
    <c:set var="assemblyFiltersNote">
      Type "<i>+:assemblyName</i>" to include or "<i>-:assemblyName</i>" to exclude assemblies to/from the code coverage.
      Each rule should start from a new line. Use asterisk (*) as a wildcard for any string<bs:help file="JetBrains+dotCover"/>
    </c:set>
    <props:multilineProperty name="${params.dotCoverFiltersKey}" className="longField" expanded="true" cols="60" rows="4" linkTitle="Assembly filters" note="${assemblyFiltersNote}"/>
    <br/>
    <c:set var="attributeFilterNote">
      Type "<i>-:attributeName</i>" to exclude any code marked with this attribute from the code coverage.
      Each rule should start from a new line. Use asterisk (*) as a wildcard for any string
    </c:set>
    <props:multilineProperty
        name="${params.dotCoverAttributeFiltersKey}"
        className="longField"
        expanded="${not empty propertiesBean.properties[params.dotCoverAttributeFiltersKey]}"
        cols="60" rows="4"
        linkTitle="Attribute filters"
        note="${attributeFilterNote}"
    />
    <span class="smallNote">
      Applicable to dotCover 2.0 or higher <bs:help file="JetBrains+dotCover"/>
    </span>
    <br/>
    <props:multilineProperty
        name="${params.dotCoverArgumentsKey}"
        linkTitle="Additional arguments"
        className="longField"
        expanded="${not empty propertiesBean.properties[params.dotCoverArgumentsKey]}"
        cols="60" rows="4"
    />
    <span class="smallNote">
      New-line separated command line parameters for dotCover cover command
    </span>
    <span id="error_${params.dotCoverArgumentsKey}" class="error"></span>
  </td>
</tr>

<script type="text/javascript">
  $j(document).ready(function() {
    const generateReportCheckbox = $j('#dotCoverGenerateReportCheckbox');
    const generateReportHiddenInput = $j('#dotCoverGenerateReportHiddenInput');
    generateReportCheckbox.change(function() {
      const isChecked = $j(this).is(':checked');
      generateReportHiddenInput.val(isChecked);
    });
  });
</script>