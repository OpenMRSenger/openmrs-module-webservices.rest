<%@ include file="/WEB-INF/template/include.jsp"%>

<style type="text/css">
	table.queryTable, table.queryTable th, table.queryTable td
	{
		border: solid 1px gray;
		  border-collapse: collapse;
	}
	
</style>

<c:forEach var="resource" items="${searchHandlersData}">
	
<h3>
  <c:out value="${resource.resourceName}" escapeXml="true" /> 
</h3>
      
     <ul>
		<li>
		   <b><spring:message code="webservices.rest.help.url"/>: </b> <c:out value="${resource.resourceURL}" escapeXml="true" />
		</li>
		<li>
		  OpenMRS versions: 
		  <c:forEach var="ver" items="${resource.supportedVersions}" varStatus="status">


                    
		            <c:set value="${status.count}" var="size"></c:set>
		             <c:out value="${ver}" escapeXml="true" />
		            <c:if test="${ status.index != size - 1 }"> ,</c:if>
		  </c:forEach>
		</li>
	    <li>
	       <spring:message code="webservices.rest.help.availableHandlers"/> :
	          <ol type="1">
	         <c:forEach var="searchQuery" items="${resource.searchQueriesDoc}">
	             <li>
	               <table class="queryTable">
	                  <tr>
	                  <th><spring:message code="webservices.rest.help.requiredParameters"/></th>
	                  <th> <spring:message code="webservices.rest.help.optionalParameters"/></th>
	                  <th> <spring:message code="webservices.rest.help.description"/></th>
	                 </tr>
	                 <tr>
	                   <td>
	                     <c:forEach var="requiredParameter" items="${searchQuery.requiredParameters}">
	                               <c:out value="${requiredParameter}" escapeXml="true" /><br>
	                     </c:forEach>
	                   </td>
	                   	<td>
	                   	 <c:forEach var="optionalParameter" items="${searchQuery.optionalParameters}">
	                              <c:out value="${optionalParameter}" escapeXml="true" /><br>
	                     </c:forEach>
	                   </td>
	                    <td>
	                    <c:out value="${searchQuery.description}" escapeXml="true" />
	                   </td>
	                  </tr>
	               </table>
	             </li>
	         </c:forEach>
	          </ol>
		</li>
    </ul>
</c:forEach>