<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:import url="header.jsp" />
<!DOCTYPE html>
<html>

<%@page import="java.util.*" %>

<head>
	<title>Query Result</title>
</head>

<body>
	<div id="chartArea" align = "center">
		<br/><h3>Below is the info according to query condition</h3>	
		
		<%
			String xValue = (String)request.getAttribute("xValue");
			String yValue = (String)request.getAttribute("yValue");		
			String totalCount = (String)request.getAttribute("TotalCount");
			String fieldInfo = (String)request.getAttribute("jobField");
			String[] fields = fieldInfo.split(",");
			
			String jobInfo = (String)request.getAttribute("fullJobInfo");
			String[] jobs = jobInfo.split(";");
			
			List<List<String>> jobDetail = new ArrayList<List<String>>();
			for (String job : jobs){
				String[] detail = job.split(",");
				
				List<String> detailList = new ArrayList<String>();
				for (String specificField : detail){
					detailList.add(specificField);
				}
				jobDetail.add(detailList);
			}
			
			request.setAttribute("fieldDetail", fields);
			request.setAttribute("jobAttr", jobDetail);
		%>

		<h4><%=totalCount %></h4><br/>
		
		<canvas id="myChart" width="1440" height="720"></canvas>
		<script>
		var aXValue = <%="\""+xValue+ "\""%>.split(",");
		var aYValue = <%="\""+yValue+ "\""%>.split(",");

		var data= {
				labels: aXValue,
				
				datasets: [
							{
								fillColor : "rgba(100,149,237,0.5)", 
								strokeColor : "rgba(220,220,220,1)", 
								data: aYValue
							}			
						]
				};
			var ctx = document.getElementById("myChart").getContext("2d");
			var myNewChart = new Chart(ctx).Bar(data);
		</script>
	</div>
	
	
	<div id="detailTable" >
		<table class="table table-striped" >
			<caption></caption>
			<thead>
				<tr>
					<c:forEach items="${fieldDetail }" var="item">
						<th><c:out value="${item }" />
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${jobAttr }" var="item">
					<tr>
						<c:forEach items="${item }" var="subitem">
							<td><c:out value="${subitem }" /></td>	
						</c:forEach>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>	
</body>
</html>