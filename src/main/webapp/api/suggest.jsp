<%@ page import="hk.ust.cse.comp4321.proj1.Main"%>
<%@ page import="org.rocksdb.RocksDBException"%>
<%@ page contentType="application/json" %>

<%
    String queryString = request.getParameter("q");
    if (queryString == null) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
        try {
            String result = Main.suggest(queryString);
            out.write(result);
        } catch (RocksDBException | ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(e.getMessage());
        }
    }
%>
