<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:import url="header.jsp" />
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Invalid Query</title>
</head>

<body>
	<br/><div id="chartArea" align = "center">
		<%
			String msg = (String)request.getAttribute("message");
		%>
		<h4><%=msg %></h4><br/>
	</div>
</body>
</html>