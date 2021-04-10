<%@ page import="hk.ust.cse.comp4321.proj1.Main"%>
<%@ page import="org.rocksdb.RocksDBException"%>
<%@ page contentType="application/json" %>

<%
    String queryString = request.getParameter("q");
    try {
        String result = Main.query(queryString);
        out.write(result);
    } catch (RocksDBException | ClassNotFoundException e) {
        response.setStatus(500);
        out.write(e.getMessage());
    }
%>