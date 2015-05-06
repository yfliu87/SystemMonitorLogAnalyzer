<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:import url="header.jsp" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>System Monitor Log Analyzer</title>
</head>

<body>
<%
	String jobCount = (String)request.getAttribute("jobCount");
%>
	<center><br/><br/><br/>
		<h1>Welcome to System Monitor Analyzer</h1><br/>
		<h4><%=jobCount %></h4>
		<h4>please input query condition below</h4><br/>
		<h6>*any query condition can be left empty</h6><br/>
		
		<sf:form class="form-horizontal" roel="form" method="POST" modelAttribute="systemMonitorQuery" >		 
			<tr align="left">
				<td>MW Version &nbsp;&nbsp;<select name="MWVersion" selected STYLE="width:220px">
						<option value="none">none</option>
						<option value="4.0.9163.3000">MW 2013</option>
						<option value="5.0.29600.3100">MW 2014</option>
					</select>
				</td>
			</tr><br/><br/>
		 	<tr align="left">
				<td>Patch Version <select name="patchVersion" selected STYLE="width:217px">
						<option value="none">None</option>
						<option value="2013 SP1 WL">2013 SP1 WL</option>
						<option value="2013 SP1 D&M">2013 SP1 D&M</option>
						<option value="2013 SP2">2013 SP2</option>
						<option value="2013 SP3">2013 SP3</option>
						<option value="2013 SP4">2013 SP4</option>
						<option value="2014 SP1">2014 SP1</option>
					</select>
				</td>
			</tr><br/><br/>
			<tr align="left">
			
				<td>Workflow <select name="workflow" selected STYLE="width:245px">
						<option value="none">none</option>
						<option value="D&M">D&M</option>
						<option value="Wireline">Wireline</option>
						<option value="WL Recorder">WL Recorder</option>
						<option value="PerfoExpress">PerfoExpress</option>
						<option value="Coiled Tubing">Coiled Tubing</option>
					</select>
				</td>
			</tr><br/><br/>	
			
			<tr align="left">
				<td>Job Start Date&nbsp;<sf:input path="jobStartDate" cssStyle="width:136px; " /> *yyyymmdd</td>
			</tr><br/><br/>
			
			<tr>
				<td align="left">Job Stop Date&nbsp;<sf:input path="jobStopDate" cssStyle="width:136px; " /> *yyyymmdd</td>
			</tr><br/><br/>
			
			<tr align="left">
				<td>Job Duration &nbsp;<sf:input path="jobDuration" cssStyle=" width: 90px; align: left"/> *unit:hour(e.g.>=2.5)</td>
			</tr><br/><br/>
			
			<tr>
				<td>Tools &nbsp;&nbsp;<sf:input path="toolString" cssStyle=" width: 252px; "/></td>
			</tr><br/><br/>
			
			<tr>
				<td>Features &nbsp;<sf:input path="features" cssStyle=" width : 233px; " value="coming soon"/></td>
			</tr><br/><br/>	
			
			<tr>
				<td>Crash &nbsp;<input type="checkbox" name="crashChecked"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					ChartType <select name="chartType" selected STYLE="width:158px" >
						<option value="BarChart">BarChart</option>
						<option value="LineChart">LineChart</option>
					</select>
				</td>
			</tr><br/><br/><br/><br/>
				
			<button type="submit" class="btn btn-primary">query</button>
		</sf:form>
	</center>
</body>
</html>