<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotCover.DotCoverRunnerParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<tr>
  <th>dotCover tool:</th>
  <td>
    <jsp:include page="/tools/selector.html?toolType=JetBrains.dotCover.CommandLineTools&versionParameterName=${params.dotCoverHomeKey}&class=longField"/>
  </td>
</tr>

<tr>
  <th>Cover:</th>
  <td>
    <props:multilineProperty name="${params.dotCoverCommandLineKey}" linkTitle="Command line (optional)" className="longField" cols="60" rows="1"/>
    <span class="smallNote">Run a process from the command line under dotCover coverage profile and produce produces a dotCover snapshot file</span>
  </td>
</tr>

<tr class="advancedSetting">
  <th>Coverage settings:</th>
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
    <props:multilineProperty name="${params.dotCoverAttributeFiltersKey}" className="longField" expanded="false" cols="60" rows="4" linkTitle="Attribute filters" note="${attributeFilterNote}"/>
    <span class="smallNote">
      Applicable to dotCover 2.0 or higher <bs:help file="JetBrains+dotCover"/>
    </span>
    <br/>
    <props:multilineProperty name="${params.dotCoverArgumentsKey}" linkTitle="Additional arguments" className="longField" expanded="false" cols="60" rows="4"/>
    <span class="smallNote">
      New-line separated command line parameters for dotCover cover command
    </span>
    <span id="error_${params.dotCoverArgumentsKey}" class="error"></span>
  </td>
</tr>

<tr>
  <th>Report:</th>
  <td>
    <props:checkboxProperty name="${params.dotCoverGenerateReportKey}" checked="${params.dotCoverGenerateReportKey}"/>
    <label for="${params.dotCoverGenerateReportKey}">Generate coverage report</label>
    <span class="smallNote">Generates a TeamCity coverage report that will be displayed on the Code Coverage tab after the build is complete</span>
    <br/>
    <c:set var="additionalSnapshotsNote">
      <span>Specify dotCover snapshot (.dcvr) files paths separated by spaces or new lines.</span>
      <bs:helpLink file="Wildcards">Wildcards</bs:helpLink> are supported. Note that you can merge snapshots generated only by the selected or earlier version of dotCover tool
    </c:set>
    <props:multilineProperty
        name="${params.dotCoverAdditionalShapshotPathsKey}"
        className="longField" expanded="false" cols="60" rows="4"
        linkTitle="Include additional dotCover snapshots to the report"
        note="${additionalSnapshotsNote}"
    />
  </td>
</tr>
