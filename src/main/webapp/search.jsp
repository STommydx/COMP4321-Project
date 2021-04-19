<%@ page import="hk.ust.cse.comp4321.proj1.Main" %>
<%@ page import="org.rocksdb.RocksDBException" %>
<%@ page import="java.util.List" %>
<%@ page import="hk.ust.cse.comp4321.proj1.QueryResultEntry" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
    <title>HKUST CSE Search</title>
</head>
<body>

<%
    String queryString = request.getParameter("q");
    List<QueryResultEntry> resultEntries = new ArrayList<>();
    if (queryString != null) {
        try {
            resultEntries = Main.queryRaw(queryString);
        } catch (RocksDBException | ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(e.getMessage());
        }
    }
%>
<table>
    <thead>
    <tr>
        <th>Similarity</th>
        <th>Document</th>
    </tr>
    </thead>
    <tbody>
    <%
        for (QueryResultEntry resultEntry : resultEntries) {
            out.write("<tr>");
            out.write("<td>");
            out.write(Double.toString(resultEntry.getSimilarity()));
            out.write("</td>");
            out.write("<td>");
            out.write(resultEntry.getDocumentRecord().toString().replace("\n", "<br />"));
            out.write("</td>");
            out.write("</tr>");
        }
    %>
    </tbody>
</table>
</body>
</html>
