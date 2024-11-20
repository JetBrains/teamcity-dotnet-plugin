<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ page import="jetbrains.buildServer.serverSide.TeamCityProperties" %>
<%@ page import="jetbrains.buildServer.cache.depcache.DependencyCacheConstants" %>

<tr>
  <td colspan="2" height="5">
    <em>
      Caches NuGet packages downloaded by .NET steps to speed up the builds.<bs:help file="Dependency+Caches"/>
    </em>
    <div style="margin-top: 10px;">
      The feature tracks NuGet <a href="https://learn.microsoft.com/en-us/nuget/consume-packages/managing-the-global-packages-and-cache-folders">global-packages</a>
      directories used by the <code>dotnet</code> command and caches packages in the artifact storage.
      The cache is automatically updated when dependencies of the corresponding .NET projects change.
      <b>NuGet package caching is supported when the build command uses .NET SDK 7.0.200 or higher.</b>
    </div>
    <c:set var="restrictedToEphemeralAgents"
           value='<%= TeamCityProperties.getBoolean(DependencyCacheConstants.DEPENDENCY_CACHE_EPHEMERAL_AGENTS_ONLY, DependencyCacheConstants.DEPENDENCY_CACHE_EPHEMERAL_AGENTS_ONLY_DEFAULT) %>'/>
    <c:choose>
      <c:when test="${restrictedToEphemeralAgents}">
        <div class="attentionComment">
          <bs:buildStatusIcon type="red-sign" className="warningIcon"/>
          Currently, NuGet caching is only performed on
          <a href="<bs:helpUrlPrefix/>predefined-build-parameters#Predefined+Agent+Environment+Parameters" target="_blank" rel="noreferrer noopener"
             showdiscardchangesmessage="false">ephemeral agents</a>
          (cloud agents terminated after their first build). Builds running on non-ephemeral agents neither cache nor reuse previously cached dependencies.
        </div>
      </c:when>
      <c:otherwise>
        <div class="attentionComment">
          <bs:buildStatusIcon type="red-sign" className="warningIcon"/>
          Package caching is most effective on short-lived agents. For long-lived or permanent cloud agents, periodically review hidden
          <code>.teamcity.build_cache</code> build artifacts to monitor cache size and contents. This helps prevent redundant dependencies and unnecessary cache bloat.
        </div>
      </c:otherwise>
    </c:choose>
  </td>
</tr>