<!doctype html>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<html>
<head>
	<link type="text/css" rel="stylesheet" href="/odp/MiniUP.css?v=45"/>
	
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
	
	<script type="text/javascript" src="/odp/javascript/script.js"></script>
	
	<title>Initium Game Site</title>

</head>

<body>
	<%JspSnippets.allowPopupMessages(out, request); %>
	
	<div>
	<a onclick='doCommand(event, "Sample", {test:1})'>Sample(1)</a>
	<a onclick='doCommand(event, "Sample", {test:2})'>Sample(2)</a>
	<a onclick='doCommand(event, "Sample", {test:3})'>Sample(3)</a>
	</div>
</body>
</html>
