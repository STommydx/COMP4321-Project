<%@ page import="hk.ust.cse.comp4321.proj1.Main" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<%
    String queryString = request.getParameter("q");
    if (queryString != null) {
        String queryResult = Main.query(queryString);
        out.print(queryResult);
    }
%>
</body>
</html>
