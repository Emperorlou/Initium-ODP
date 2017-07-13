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
		<div>
			<div class='autofix-entry'>
				<a class="big-link" onclick='autofixStuckInLocation(event)'> Stuck In Location</a>
			</div>
			<div class='paragraph'>Use this if you're stuck with no discoverable paths out of your current location.</div>
		</div>
		<div>
			<div class='autofix-entry'>
				<a class="big-link" onclick='autofixDeathModeNotSet(event)'> Undead Character</a>
			</div>
			<div class='paragraph'>Use this if your character has negative HP but is not showing up as "Dead" or "Unconscious".</div>
		</div>
		<h2>Other Fixes</h2>
		<div>
			<div class='autofix-entry'>
				<a class="big-link" onclick='autofixLootStuckOnMonster(event)'> Loot Stuck on Monster</a>
			</div>
			<div class='paragraph'>Use this is you've killed a monster but its loot is stuck to the corpse (only works in combat sites).</div>
		</div>
	</div>
</body>
</html>

