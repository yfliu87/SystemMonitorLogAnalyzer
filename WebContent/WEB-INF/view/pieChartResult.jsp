<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<script type="text/javascript" src="https://rawgit.com/nnnick/Chart.js/master/Chart.js"></script>
	<title>Query Result</title>
</head>

<body>	
	<div id="chartArea" align = "center">

		<h3>Below is the info according to query condition</h3>
		<c:forEach items="${result }" var="job">
			${job}<br/>
		</c:forEach>
		<br/>
	
		<canvas id="myChart" width="1024" height="768"></canvas>
		
		<%
			String xValue = (String)request.getAttribute("xValue");
			String yValue = (String)request.getAttribute("yValue");			
		%>

		<script>
			var aXValue = <%="\""+xValue+ "\""%>.split(",");
			var aYValue = <%="\""+yValue+ "\""%>.split(",");
		
			var data= [
		   		{
					value: 30,
					color:"#F38630"
			   	},
			   	{
				   	value: 50,
				   	color: "E0E4CC"
				},
				{
					value: 100,
					color: "#69D2E7"
				}
				];
			var ctx = document.getElementById("myChart").getContext("2d");
			var myNewChart = new Chart(ctx).Pie(data);
		</script>
	</div>
</body>
</html>