<%@ page import="Database.*" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" session="true" %>
<%@ page import="java.net.*, java.io.*, java.util.*, java.text.*, javax.servlet.*, javax.servlet.http.*, org.json.*, org.rocksdb.*, org.rocksdb.util.*" %> 

<html>
<head>
<title> Query History Page</title>
<style>
.query{
	margin-top:5px;
}
</style>

<strong style="font-size:40px">Query History</strong><br>
<strong>
	You may click to review the search result
</strong>
<br>
<%
Database.Search s = new Database.Search();
String[] queryHistoryArray = s.print_query_history();
%>

<%
int numHistory = queryHistoryArray.length;
for(int i=0; i<numHistory; i++){
%>
<div class="query">
>
	<a href="afterSubmit.jsp?query=<%= queryHistoryArray[i] %>">
	<%= queryHistoryArray[i] %>
	</a>
</div>
<% } %>