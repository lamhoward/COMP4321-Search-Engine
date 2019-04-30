<%@ page import="Database.*" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" session="true" %>
<%@ page import="java.net.*, java.io.*, java.util.*, java.text.*, javax.servlet.*, javax.servlet.http.*, org.json.*, org.rocksdb.*, org.rocksdb.util.*" %> 

<html>
<head>
<strong style="font-size:40px">Keyword List</strong>
<style>
.submitB{
    border: none;
    padding: 10px;
    margin: 10px;
    height: 40px;
    width: 100px;
    border:1px solid #eaeaea;
	outline:none;
	border-radius: 10px;
	box-shadow: 1px 1px 1px #888888;
}
.box {
	margin-left: 10px;
	margin-top: 5px;
	margin-bottom: 5px;
}
</style>

<%
Database.Search s = new Database.Search();
String[] keywordArray = s.print_keyword();
%>

<title>Keyword List (with document frequency >10)</title>
<form method=POST ACTION=afterSubmit.jsp>
<div>
	<strong>
	You may tick the keywords you like and submit as a query to see search result
	</strong>
	<input type=submit class="submitB" name=submit Value="Submit"><br>
</div>

<%
	String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	for(int i=0; i<26; i++){
%>
	<strong style="margin-left:10px"><%= letters.charAt(i) %></strong><br>
<%	
	String keywordList = keywordArray[i];
	String[] keywords = keywordList.split(" ");
	int numKeyword = keywords.length;
	for(int j=0; j<numKeyword; j++){
%>
<input class="box" type="checkbox" name="selectedKeyword" value=<%= keywords[j] %>><%= keywords[j] %>
<% } %>
<br><br>
<% } %>
</form>