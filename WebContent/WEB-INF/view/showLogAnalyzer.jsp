<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>System Monitor Log Analyzer</title>
</head>
<body>
<center>
<h1>Welcome to System Monitor Analyzer</h1>
<h4>please input query condition below</h4>
<sf:form method="POST" modelAttribute="systemMonitorQuery">

	ToolString: <sf:input path="toolString"/><br/>
	Job Start Date: <sf:input path="jobStartDate"/><br/>
	Job Stop Date: <sf:input path="jobStopDate"/><br/>
	Job Duration: <sf:input path="jobDuration"/><br/>
	Features: <sf:input path="features"/><br/>
	
	<input type="submit" value="query"/>
</sf:form>
</center>


<!-- 
	<center>
		<h1>Welcome to System Monitor Analyzer</h1>
		<h4>please input query condition below</h4>
			<form name="queryConditionForm" action="queryLog" method="POST">
				<table>
					<tr>
						<td align="center">
							Tool &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text"/>
						</td>
					</tr>
					<tr>
						<td align="center">
							Job Date &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text"/>
						</td>
					</tr>
					<tr>
						<td align="center">
							Date Range <input type="text"/>
						</td>
					</tr>
					<tr>
						<td align="center">
							Feature &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" />
						</td>
					</tr>
					<tr>
						<td>
							Crash or Not <input type="checkbox"/>
						</td>
					</tr>
					<tr>
						<td align="center">
							<input type="submit" onclick="addForm()" value="Query"/>
						</td>
					</tr>
				</table>
			</form>
		</center> -->
</body>
</html>