<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<tr>
    <th><label for="${params.nugetDeleteSourceKey}">NuGet Server: <l:star/></label></th>
    <td>
        <props:textProperty name="${params.nugetDeleteSourceKey}" className="longField"/>
        <span class="error" id="error_${params.nugetDeleteSourceKey}"></span>
        <span class="smallNote">Specify the server URL.</span>
    </td>
</tr>

<tr>
    <th class="noBorder"><label for="${params.nugetDeleteIdKey}">Package ID: <l:star/></label></th>
    <td>
        <props:textProperty name="${params.nugetDeleteIdKey}" className="longField"/>
        <span class="error" id="error_${params.nugetDeleteIdKey}"></span>
        <span class="smallNote">Specify the package id and version separated by space.</span>
    </td>
</tr>

<tr>
    <th class="noBorder"><label for="${params.nugetDeleteApiKey}">API key: <l:star/></label></th>
    <td>
        <props:passwordProperty name="${params.nugetDeleteApiKey}" className="longField"/>
        <span class="error" id="error_${params.nugetDeleteApiKey}"></span>
        <span class="smallNote">Specify the API key to access a NuGet packages feed.</span>
    </td>
</tr>