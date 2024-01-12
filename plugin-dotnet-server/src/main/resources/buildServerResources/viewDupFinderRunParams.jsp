

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="constants" class="jetbrains.buildServer.inspect.DupFinderConstantsBean"/>

<div class="parameter">
  <ul style="list-style: none; padding-left: 0; margin-left: 0; margin-top: 0.1em; margin-bottom: 0.1em;">
    <li>Include patterns: <strong><props:displayValue name="${constants.includeFilesKey}" emptyValue="not specified"/></strong></li>
    <li>Exclude patterns: <strong><props:displayValue name="${constants.excludeFilesKey}" emptyValue="not specified"/></strong></li>
  </ul>
</div>

<div class="parameter">
  Path to dupFinder home: <jsp:include page="/tools/selector.html?name=${constants.cltToolTypeName}&class=longField&view=1"/>
</div>

<div class="parameter">
  Code fragments comparison:
</div>

<div class="nestedParameter">
  <ul style="list-style: none; padding-left: 0; margin-left: 0; margin-top: 0.1em; margin-bottom: 0.1em;">
    <li>Discard namespaces: <strong><props:displayCheckboxValue name="${constants.normalizeTypesKey}"/></strong></li>
    <li>Discard types name: <strong><props:displayCheckboxValue name="${constants.discardTypesKey}"/></strong></li>
    <li>Discard class fields name: <strong><props:displayCheckboxValue name="${constants.discardFieldsNameKey}"/></strong></li>
    <li>Discard local variables name: <strong><props:displayCheckboxValue name="${constants.discardLocalVariablesNameKey}"/></strong></li>
    <li>Discard literals: <strong><props:displayCheckboxValue name="${constants.discardLiteralsKey}"/></strong></li>
  </ul>
</div>

<div class="parameter">
  Ignore duplicates with complexity lower than: <strong><props:displayValue name="${constants.discardCostKey}" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Skip files by opening comment: <strong><props:displayValue name="${constants.excludeByOpeningCommentKey}" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Skip regions by message substring: <strong><props:displayValue name="${constants.excludeRegionMessageSubstringsKey}" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Enable debug messages: <strong><props:displayCheckboxValue name="${constants.debugKey}"/></strong>
</div>

<div class="parameter">
  Additional dupFinder parameters: <strong><props:displayValue name="${constants.customCommandlineKey}" showInPopup="${true}"/></strong>
</div>