<%--

    Copyright © 2018 Instituto Superior Técnico

    This file is part of FenixEdu Spaces.

    FenixEdu Spaces is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu Spaces is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu Spaces.  If not, see <http://www.gnu.org/licenses/>.

--%>

<!DOCTYPE html> 
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div class="page-header">
    <h2>
        <spring:message code="label.renates.title"/>
    </h2>
</div>
<c:if test="${!errors.isEmpty()}">
	<div class="error0">
	<table>
		<c:forEach var="error" items="${errors}">      
			<tr>
				<td class="error0"> <spring:message code="${error}"></spring:message> </td>
			</tr>
		</c:forEach>
	</table>
	</div>
	<br>
	<br>
</c:if>



<p> 
	<strong> <spring:message code="label.renates.report.title"/> </strong> 
</p>

<c:if test="${not doneQueuejobs.isEmpty()}">
	<table class="tstyle1 thlight thright mtop025 table">
	    <thead>
	        <th><spring:message code="label.renates.report.date.request" /></th>
	        <th><spring:message code="label.renates.report.date.job.finish"/></th>
			<th><spring:message code="label.renates.report.requestor"/></th>
	        <th></th>
	        
	    </thead>
	    <tbody>
	        <c:forEach var="queuejob" items="${doneQueuejobs}">               
		    	<tr>
	        		<td>
						<c:out value="${queuejob.requestDate.toString(\"dd-MM-yyyy kk:mm:ss\")}" />                    
	        		</td>
	        		<td>
	        			<c:out value="${queuejob.jobEndTime.toString(\"dd-MM-yyyy kk:mm:ss\")}"/>
	        		</td>
	        		<td>
	        			<c:out value="${queuejob.person.name}"/>
	        		</td>	        		
	        		<td>	                	            	
						<spring:url var="downloadExcel" value="/renates-integration/download/renates-report/${queuejob.externalId}" />
						<a href="${downloadExcel}"><spring:message code="label.renates.report.view"/></a>						
	        		</td>
	        	</tr>            
	        </c:forEach>
	    </tbody>    
	</table>
</c:if>

<c:if test="${doneQueuejobs.isEmpty()}">
	<em> <spring:message code="label.renates.report.no.requests.made"/></em>
	<br>
	<br>
</c:if>

<p> 
	<strong> <spring:message code="label.renates.request.title"/> </strong>
</p>

<c:if test="${not undoneQueuejobs.isEmpty()}">
	<table class="table results">
	    <thead>
	        <th><spring:message code="label.renates.request.date.request"/></th>
	        <th><spring:message code="label.renates.request.requestor"/></th>       	        
	    </thead>
	    <tbody>
	        <c:forEach var="queuejob" items="${undoneQueuejobs}">               
		    	<tr>
	        		<td>
						<c:out value="${queuejob.requestDate.toString(\"dd-MM-yyyy kk:mm:ss\")}" />                    
	        		</td>
	        		<td>
	        			<c:out value="${queuejob.person.name}"/>
	        		</td>	        			                	            	        
	        </c:forEach>
	    </tbody>    
	</table>
</c:if>

<c:if test="${undoneQueuejobs.isEmpty()}">
	<em> <spring:message code="label.renates.request.no.requests.made"/></em>
</c:if>

<p> 
	<spring:url var="requestReport" value="/renates-integration/requestRenatesReport"/>
	<a href="${requestReport}"><spring:message code="label.renates.request.create"/></a>
</p>
