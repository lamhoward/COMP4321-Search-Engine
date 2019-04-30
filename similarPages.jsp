<%@ page import="Database.*" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" session="true" %>
<%@ page import="java.net.*, java.io.*, java.util.*, java.text.*, javax.servlet.*, javax.servlet.http.*, org.json.*, org.rocksdb.*, org.rocksdb.util.*" %> 

<html>
<head>
<title> Similar Result Page</title>
<style>
form {
	width: 100%;
	margin: 0 auto; 
}
.container {
	display: flex;
	flex-direction: row;
	justify-content: flex-start;
	align-items:center;
}

input{
    border: none;
    padding: 10px;
    margin: 10px;
    height: 40px;
    width: 450px;
    border:1px solid #eaeaea;
	outline:none;
	border-radius: 10px;
	box-shadow: 1px 1px 1px #888888;
}

input:hover{
    border-color: #a0a0a0 #b9b9b9 #b9b9b9 #b9b9b9;
}
input:focus{
    border-color:#4d90fe;
}

input[type="submit"] {
    border-radius: 2px;
    background: #f2f2f2;
    border: 1px solid #f2f2f2;
    color: #757575;
    cursor: default;
    font-size: 14px;
    font-weight: bold;
    width: 100px;
    padding: 0 16px;
	height:40px;
	border-radius: 10px;
}
input[type="submit"]:hover {
    box-shadow: 0 1px 1px rgba(0,0,0,0.1);
    background: #f8f8f8;
    border: 1px solid #c6c6c6;
    box-shadow: 0 1px 1px rgba(0,0,0,0.1);
	color: #222;
	border-radius: 10px;
}
.textbox {
	font-size: 20px;
	text-align: center;
	width: 150px;
	padding-top: 8px;
	cursor: pointer;
}
.topbar{
	display: flex;
	justify-content: flex-start;
	direction: row;
	background-color: #fff; 
}
.resultbox{
	display: flex;
	direction: row;
	margin-top: 30px;
}
.score{
	width : 100px;
	text-align: center;
}
.example_e {
border: none;
background: #404040;
color: #ffffff !important;
font-weight: 100;
padding: 10px;
border-radius: 6px;
display: inline-block;
margin-left: 20px;
margin-top: 10px;
height:20px;
}
.example_e:hover {
color: #404040 !important;
font-weight: 200 !important;
letter-spacing: 1px;
background: none;
-webkit-box-shadow: 0px 5px 40px -10px rgba(0,0,0,0.57);
-moz-box-shadow: 0px 5px 40px -10px rgba(0,0,0,0.57);
transition: all 0.2s ease 0s;
}
</style>
</head>
<body>
<%
String query = "";
query = session.getAttribute("queryO").toString();
Database.Search s = new Database.Search();
String[][] results = s.query(query,50);
String[][] similarPages = s.similarPage();
%>

<div class="topbar">
	<div onclick="window.open('form.html','_self')" class="textbox">
		<b>COMP4321<br>Search Engine</b>
	</div>
	<div class="container">
		<form method="post" action="afterSubmit.jsp"> 
			<input type="text" name="txtname" value="<%= query%>" onfocus="value=''"> 
			<input type="submit" value="Submit" style="cursor:pointer"> 
		</form> 
	</div>
	<div class="button_cont">
		<a class="example_e" href="keywordList.jsp" target="_blank" rel="nofollow noopener">
			see keyword list
		</a>
	</div>
	<div class="button_cont">
		<a class="example_e" href="queryHistory.jsp" target="_blank" rel="nofollow noopener">
			see query history
		</a>
	</div>
</div>

<% 	if(similarPages!=null){
	for(int i = 0; i<50; i+=1){ 
	if(similarPages[i][0]==null){
		continue;
	}%>
	<div class = "resultbox">
		<div class = "score">
			<%=similarPages[i][0]%>
		</div>
		<div class="otherwebinfo">
			<text class="pagetitle" onclick="window.open('<%=similarPages[i][2]%>')" style="font-size: 20px; color:blue; cursor:pointer">
				<%=similarPages[i][1]%><br>
			</text>
			<text class="url" onclick="window.open('<%=similarPages[i][2]%>')" style="color:green; text-decoration:underline; cursor:pointer;">
				<%=similarPages[i][2]%><br>
			</text>
			<text class="dataandsize" style="color:#888888">
				<%=similarPages[i][3]%><br>
				<%=similarPages[i][4]%><br>
			</text>
			<text>
				<strong>Top 5 Frequent Keywords (after stemming):</strong><br>
				<% if(similarPages[i][5]!=null&&similarPages[i][6]!=null){ %>
				<%=similarPages[i][5]%>-<%=similarPages[i][6]%> ;
				<% } %>
				<% if(similarPages[i][7]!=null&&similarPages[i][8]!=null){ %>
				<%=similarPages[i][7]%>-<%=similarPages[i][8]%> ;
				<% } %>
				<% if(similarPages[i][9]!=null&&similarPages[i][10]!=null){ %>
				<%=similarPages[i][9]%>-<%=similarPages[i][10]%> ;
				<% } %>
				<% if(similarPages[i][11]!=null&&similarPages[i][12]!=null){ %>
				<%=similarPages[i][11]%>-<%=similarPages[i][12]%> ;
				<% } %>
				<% if(similarPages[i][13]!=null&&similarPages[i][14]!=null){ %>
				<%=similarPages[i][13]%>-<%=similarPages[i][14]%>
				<% } %>
				<br>
			</text>
			<text class="links">
				<strong>Parent Links (show up to 5):</strong><br>
				<% if(similarPages[i][15]!=null){ %>
				<%=similarPages[i][15]%><br>
				<% } %>
				<% if(similarPages[i][16]!=null){ %>
				<%=similarPages[i][16]%><br>
				<% } %>
				<% if(similarPages[i][17]!=null){ %>
				<%=similarPages[i][17]%><br>
				<% } %>
				<% if(similarPages[i][18]!=null){ %>
				<%=similarPages[i][18]%><br>
				<% } %>
				<% if(similarPages[i][19]!=null){ %>
				<%=similarPages[i][19]%><br>
				<% } %>
				<% if(similarPages[i][15]==null&&similarPages[i][16]==null&&similarPages[i][17]==null&&similarPages[i][18]==null&&similarPages[i][19]==null){ %>
				there is no parent link<br>
				<% } %>
			</text>
			<text class="links">
				<strong>Child Links (show up to 5)</strong><br>
				<% if(similarPages[i][20]!=null){ %>
				<%=similarPages[i][20]%><br>
				<% } %>
				<% if(similarPages[i][21]!=null){ %>
				<%=similarPages[i][21]%><br>
				<% } %>
				<% if(similarPages[i][22]!=null){ %>
				<%=similarPages[i][22]%><br>
				<% } %>
				<% if(similarPages[i][23]!=null){ %>
				<%=similarPages[i][23]%><br>
				<% } %>
				<% if(similarPages[i][24]!=null){ %>
				<%=similarPages[i][24]%><br>
				<% } %>
				<% if(similarPages[i][20]==null&&similarPages[i][21]==null&&similarPages[i][22]==null&&similarPages[i][23]==null&&similarPages[i][24]==null){ %>
				there is no child link<br>
				<% } %>
				</text>
		</div>
	</div>
<% }
}
else{ %>
	<text> sorry there is no result</text>
<%	}
%>

</body>
</html>
