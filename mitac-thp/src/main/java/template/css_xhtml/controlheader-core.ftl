<#--
/*
 * $Id: controlheader-core.ftl 1423607 2012-12-18 19:53:12Z lukaszlenart $
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
-->
<#--
	Only show message if errors are available.
	This will be done if ActionSupport is used.
-->
<#assign hasFieldErrors = parameters.name?? && fieldErrors?? && fieldErrors[parameters.name]??/>
<div <#rt/><#if parameters.id??>id="wwgrp_${parameters.id}"<#rt/></#if> class="wwgrp form-group right-inner-addon col-md-6">
	
<#if parameters.errorposition?default("top") == 'top'>
<#if hasFieldErrors>
<div <#rt/><#if parameters.id??>id="wwerr_${parameters.id}"<#rt/></#if> class="wwerr">
<#list fieldErrors[parameters.name] as error>
    <div<#rt/>
    <#if parameters.id??>
     errorFor="${parameters.id}"<#rt/>
    </#if>
    class="errorMessage">
             ${error?html}
    </div><#t/>
</#list>
</div><#t/>
</#if>
</#if>

<#if parameters.label??>
<#if parameters.labelposition?default("top") == 'top'>
<div <#rt/>
<#else>
<span <#rt/>
</#if>
<#if parameters.id??>id="wwlbl_${parameters.id}"<#rt/></#if> class="wwlbl col-md-4">
    <label <#t/>
<#if parameters.id??>
        for="${parameters.id?html}" <#t/>
</#if>
<#if hasFieldErrors>
        class="errorLabel"<#t/>
<#else>
        class="control-label"<#t/>
</#if>
    ><#t/>
<#if parameters.required?default(false) && parameters.requiredPosition?default("right") != 'right'>
        <span class="required">*</span><#t/>
</#if>
${parameters.label?html}
<#if parameters.required?default(false) && parameters.requiredPosition?default("right") == 'right'>
        <span class="required">*</span><#t/>
</#if>
${parameters.labelseparator!":"?html}
<#include "/${parameters.templateDir}/xhtml/tooltip.ftl" />
	</label><#t/>
<#if parameters.labelposition?default("top") == 'top'>
</div><#rt/>
<#else>
</span> <#rt/>
</#if>
</#if>
