<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>

<!doctype html>
<html>
<head>
<style>
.autofix-entry
{
	height: 38px;
	font-size: 24px;
	text-shadow: 2px 2px 3px rgba(0, 0, 0, 1);
	padding-top: 14px;
	display:block;
	color:#CCCCCC;
	text-decoration: none;
	max-width:100%;
	margin-left:30px;
}
</style>
</head>
<body>
	<div class='settings-page'>
		<h2>Character Fixes</h2>
		<div class='autofix-entry'><a onclick='characterAutofixStuckInLocation(event)'> Stuck in Location</a>
			<div class='paragraph'>Use this tool if your character is stuck in a location with no discoverable exit paths</div>
		</div>
	</div>
</body>
</html>

