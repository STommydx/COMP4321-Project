<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>HKUST CSE Search</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/search.jsp" method="get">
    <input type="text" name="q">
    <input type="submit" value="Submit">
</form>
</body>
</html>
