<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<tr>
  <td colspan="2" height="5">
    <em>
      Caches NuGet packages downloaded by .NET steps to speed up the builds.<bs:help file="Dependency+Caches"/>
    </em>
    <div style="margin-top: 10px;">
      The feature tracks NuGet <a href="https://learn.microsoft.com/en-us/nuget/consume-packages/managing-the-global-packages-and-cache-folders">global-packages</a>
      directories used by the <code>dotnet</code> command and caches packages in the artifact storage.
      The cache is automatically updated when dependencies of the corresponding .NET projects change.
      <b>NuGet package caching is supported when the build command uses .NET SDK 7.0.200 or higher.</b>.
    </div>
    <div class="attentionComment">
      <bs:buildStatusIcon type="red-sign" className="warningIcon"/>
      Currently, NuGet caching is only performed on ephemeral agents (agents terminated after their first build). Builds running on non-ephemeral agents neither cache nor reuse previously cached dependencies.
    </div>
  </td>
</tr>