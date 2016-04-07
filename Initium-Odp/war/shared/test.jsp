<!doctype html>
<!-- The DOCTYPE declaration above will set the     -->
<!-- browser's rendering engine into                -->
<!-- "Standards Mode". Replacing this declaration   -->
<!-- with a "Quirks Mode" doctype is not supported. -->

<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<html>
<head>
<link type="text/css" rel="stylesheet" href="/shared/MiniUP.css?v=45">

<!--                                           -->
<!-- Any title is fine                         -->
<!--                                           -->
<title>Initium Game Site</title>

</head>

<!--                                           -->
<!-- The body can have arbitrary html, or      -->
<!-- you can leave the body empty if you want  -->
<!-- to create a completely dynamic UI.        -->
<!--                                           -->
<body>
	<%JspSnippets.allowPopupMessages(out, request); %>
	
	<div>
	<a onclick='doCommand(event, "Sample", {test:1})'>Sample(1)</a>
	<a onclick='doCommand(event, "Sample", {test:2})'>Sample(2)</a>
	<a onclick='doCommand(event, "Sample", {test:3})'>Sample(3)</a>
	</div>
</body>
</html>
