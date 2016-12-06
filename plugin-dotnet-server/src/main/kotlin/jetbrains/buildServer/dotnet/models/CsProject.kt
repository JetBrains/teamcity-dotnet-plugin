/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.models

import javax.xml.bind.annotation.*

/**
 * Represents csproj model.
 */
@XmlRootElement(namespace = "http://schemas.microsoft.com/developer/msbuild/2003", name = "Project")
class CsProject {
    @get:XmlAttribute(name = "ToolsVersion")
    var ToolsVersion: String? = null
    @XmlElement(name = "PropertyGroup")
    var PropertyGroups: List<CsPropertyGroup>? = null
    @XmlElement(name = "ItemGroup")
    var ItemGroups: List<CsItemGroup>? = null
}

/**
 * Property group.
 */
@XmlRootElement(name = "PropertyGroup")
class CsPropertyGroup {
    @XmlElement var TargetFramework: String? = null
    @XmlElement var TargetFrameworks: String? = null
}

/**
 * Item group.
 */
@XmlRootElement(name = "ItemGroup")
class CsItemGroup {
    @XmlElement(name = "PackageReference")
    var PackageReferences: List<CsPackageReference>? = null
}

/**
 * Package reference.
 */
@XmlRootElement(name = "PackageReference")
class CsPackageReference {
    @get:XmlAttribute(name = "Include")
    var Include: String? = null
}
