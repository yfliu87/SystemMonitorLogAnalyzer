<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:import url="header.jsp" />
<!DOCTYPE html>
<html>
<%@page import="java.util.*" %>
<head>
	<title>Radar Chart Query Result</title>
</head>

<body>
	<div id="barchartArea" align = "center">
		<br/><h3>Below is the info according to query condition</h3>	
		
		<%
			String totalCount = (String)request.getAttribute("TotalCount");
			String month = (String)request.getAttribute("month");
			String monthWithProcessHeader = (String)request.getAttribute("monthWithProcessHeader");
			String monthCount = (String)request.getAttribute("monthCount");		
			String monthDetail = (String)request.getAttribute("monthDetail");
			
			String[] detail = monthDetail.split(";");
			ArrayList<List<String>> monthlyDetail = new ArrayList<List<String>>();
			for (String d : detail){
				String[] ds = d.split(",");
				ArrayList<String> lineDetail = new ArrayList<String>();
				
				for (String s : ds){
					lineDetail.add(s);
				}
				monthlyDetail.add(lineDetail);	
			}
			request.setAttribute("months", monthWithProcessHeader.split(","));
			request.setAttribute("monthlyDetail", monthlyDetail);
		%>

		<h4><%=totalCount %></h4><br/>
		
		<canvas id="barChartArea_monthlyCrashCount" width="1024" height="768"></canvas>
		<script>
		var aXValue = <%="\""+month+ "\""%>.split(",");
		var aYValue = <%="\""+monthCount+ "\""%>.split(",");

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
			var barctx = document.getElementById("barChartArea_monthlyCrashCount").getContext("2d");
			var barChart = new Chart(barctx).Bar(data);
			var barLegend = barChart.generateLegend();
		</script>
	</div><br/><br/><br/>
		
		
	<div id="detailTable" >
		<table class="table table-striped" >
			<caption></caption>
			<thead>
				<tr>
					<c:forEach items="${months }" var="item">
						<th><c:out value="${item }" />
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${monthlyDetail }" var="item">
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