<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%--
  ~ Copyright 2000-2023 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotCover.DotCoverParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<script type="text/javascript">
  BS.DotCoverForm = BS.DotCoverForm || {
    updateContentBasedOnCheckbox(checkboxId, contentValueClass) {
      const advancedHiddenCoreClass = ".advanced_hidden";
      const isChecked = $j(BS.Util.escapeId(checkboxId)).is(":checked");
      const $content = $j(BS.Util.escape(contentValueClass)).not(advancedHiddenCoreClass);
      if (isChecked) {
        $content.show();
      } else {
        $content.hide();
      }
    },
    updateContentBasedOnSelect(selectId, options) {
      const selectedValue = $j(BS.Util.escapeId(selectId)).val();

      const hideAll = () => {
        for (const option of options) {
          $j(BS.Util.escapeId(option)).hide();
        }
      };

      const show = (option) => $j(BS.Util.escapeId(option)).show();

      for (const option of options) {
        if (selectedValue === option) {
          hideAll();
          show(option);
          BS.MultilineProperties.updateVisible();
          return;
        }
      }

      hideAll();
      show(options.first());
      BS.MultilineProperties.updateVisible();
    }
  };
</script>

<c:set var="commandTitle">Command:<bs:help file="${paramHelpUrl}BuildRunnerOptions"/></c:set>
<props:selectSectionProperty name="${params.commandKey}" title="${commandTitle}" note="">
  <c:forEach items="${params.commands}" var="type">
    <props:selectSectionPropertyContent value="${type.name}" caption="${type.description}">
      <jsp:include page="${teamcityPluginResourcesPath}/dotnet/${type.editPage}"/>
    </props:selectSectionPropertyContent>
  </c:forEach>
</props:selectSectionProperty>


<script>
  BS.UnrealRunner.updateContentBasedOnSelect('${component.parameter.name}', ${component.parameter.optionNamesAsJsArray});
</script>
