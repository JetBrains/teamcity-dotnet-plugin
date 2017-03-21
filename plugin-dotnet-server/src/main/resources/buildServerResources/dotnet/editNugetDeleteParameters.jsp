<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<tr>
    <th class="noBorder"><label for="${params.nugetDeleteIdKey}">Package ID:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.nugetDeleteIdKey}" className="longField"/>
        </div>
        <span class="error" id="error_${params.nugetDeleteIdKey}"></span>
        <span class="smallNote">Specify the package id and version separated by space.</span>
    </td>
</tr>

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
        <span class="smallNote">Specifies the server URL. Leave blank to let NuGet decide what package repository to use.</span>
    </td>
</tr>