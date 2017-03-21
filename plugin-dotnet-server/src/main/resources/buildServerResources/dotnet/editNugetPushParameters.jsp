<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.paths["nuget push"] = "Packages";
</script>

<tr>
    <th class="noBorder"><label for="${params.nugetApiKeyKey}">API key:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.nugetApiKeyKey}" className="longField"/>
        </div>
        <span class="error" id="error_${params.nugetApiKeyKey}"></span>
        <span class="smallNote">Specify the API key to access a NuGet packages feed.<br/>
            For built-in TeamCity NuGet server, specify <em>%teamcity.nuget.feed.api.key%</em>.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.nugetSourceKey}">Source:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.nugetSourceKey}" className="longField"/>
        </div>
        <span class="error" id="error_${params.nugetSourceKey}"></span>
        <span class="smallNote">Specifies the server URL. Leave blank to let NuGet decide what package repository to use.<br/>
            For built-in TeamCity NuGet server, specify <em>%teamcity.nuget.feed.server%</em> for guest-visible package source.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th>Options:</th>
    <td>
        <props:checkboxProperty name="${params.nugetPushNoSymbolsKey}"/>
        <label for="${params.nugetPushNoSymbolsKey}">Do not publish existing nuget symbols package</label><br/>
        <props:checkboxProperty name="${params.nugetPushNoBufferKey}"/>
        <label for="${params.nugetPushNoBufferKey}">Disable buffering when pushing to server to decrease memory usage</label>
    </td>
</tr>