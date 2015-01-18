<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Query Result</title>
</head>
<body>
<center>
	<!-- need to make the result page more fancy using graph -->
	<h3>Below is the info according to query condition</h3>
	<c:forEach items="${result }" var="job">
		${job}<br/>
	</c:forEach>
</center>
</body>
</html>