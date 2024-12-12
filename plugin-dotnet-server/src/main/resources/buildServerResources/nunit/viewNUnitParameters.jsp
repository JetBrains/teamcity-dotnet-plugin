<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags"%>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="bean" class="jetbrains.buildServer.nunit.NUnitBean"/>

<div class="parameter">
  NUnit runner:
</div>

<c:if test="${not empty propertiesBean.properties[bean.NUnitPathKey]}">
  <div class="parameter">
    Path to NUnit console tool: <strong><props:displayValue name="${bean.NUnitPathKey}"/></strong>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[bean.NUnitConfigFileKey]}">
  <div class="parameter">
    Path to application configuration file: <strong><props:displayValue name="${bean.NUnitConfigFileKey}"/></strong>
  </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[bean.NUnitCommandLineKey]}">
  <div class="parameter">
    Additional command line parameters to the NUnit console tool: <strong><props:displayValue name="${bean.NUnitCommandLineKey}"/></strong>
  </div>
</c:if>

<div class="parameter">
  Run tests from: <strong><props:displayValue name="${bean.NUnitIncludeKey}" emptyValue="none specified"/></strong>
</div>

<div class="parameter">
  Do not run tests from: <strong><props:displayValue name="${bean.NUnitExcludeKey}" emptyValue="none specified"/></strong>
</div>

<div class="parameter">
  Include categories: <strong><props:displayValue name="${bean.NUnitCategoryIncludeKey}" emptyValue="none specified"/></strong>
</div>

<div class="parameter">
  Exclude categories: <strong><props:displayValue name="${bean.NUnitCategoryExcludeKey}" emptyValue="none specified"/></strong>
</div>

