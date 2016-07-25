<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.google.appengine.api.datastore.Key"%>
<%@page import="java.util.List"%>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.universeprojects.miniup.server.ODPDBAccess"%>
<%
response.setHeader("Access-Control-Allow-Origin", "*");		// This is absolutely necessary for phonegap to work
if (request.getServerName().equals("www.playinitium.appspot.com")
|| request.getServerName()
.equals("playinitium.appspot.com")) {
response.setStatus(301);
response.setHeader("Location", "http://www.playinitium.com");
return;
}
%>

<!doctype html>
<!-- The DOCTYPE declaration above will set the     -->
<!-- browser's rendering engine into                -->
<!-- "Standards Mode". Replacing this declaration   -->
<!-- with a "Quirks Mode" doctype is not supported. -->

<html>
<head>
	<jsp:include page="common-head.jsp" />

	<!--                                           -->
	<!-- Any title is fine                         -->
	<!--                                           -->
	<title>Game Mechanics</title>

	<style>
		.boldbox a img {
			border: solid 3px #DDDDDD;
			max-width: 250px;
			float: left;
			margin: 10px;
		}
		p
		{
			margin-left:15px;
		}
	</style>

</head>

<!--                                           -->
<!-- The body can have arbitrary html, or      -->
<!-- you can leave the body empty if you want  -->
<!-- to create a completely dynamic UI.        -->
<!--                                           -->
<body>
	<div class='main-page'>

		<div class='main-banner-textonly'>
			<img src="/images/banner-backing.jpg" border="0" style="width:100%">
			<div class='main-banner-text'>			
				<h1>INITIUM GAME MECHANICS</h1>
				<p>
					This page will give you some in-depth insight into how the game works so you can
					get the most out of your characters. If you are just starting out, you might want
					to check out the <a href='about.jsp'>about page</a>, or the <a href='quickstart.jsp'>quick start guide</a> first.
				</p>		
			</div>
		</div>
		
		
		<h2>Weapon damage dice rolls</h2>
		<p>
			You may notice that weapon damage is represented as a dice roll. For example, 1d6 means a single 6 sided dice. The weapon damage from a 1d6 weapon is randomly generated to be any number between 1 and 6 as a result. A 3d10 weapon means 3 10 sided dice will be rolled which would mean the weapon has a minimum damage of 3 and a maximum damage of 30.
		</p>

		<h2>Character stats</h2>
		<p>As of now stats cap are random, the minimum being 9/8/8 (Strength / Dexterity / Intelligence) and the maximum being 11/10/10.

			<h4>Strength</h4>
			<p>
				<ul>
					<li>For every strength point you have, you get +2 hitpoints</li>
					<li>Strength increases the weight you can carry</li>
					<li>It increases the amount of damage you do per hit</li>
					<li>Equipments have strength requirements.</li>
					<li><i>(Not yet implemented)</i> It will determine how quickly you get exhausted (too much combat and/or moving around will require rest periods)</li>
				</ul>
			</p>

			<h4>Dexterity</h4>
			<p>
				This is used to determine the odds of a hit or a miss taking place.
				The way it works is this: If you are attacking a monster, you
				generate a random number from 0 to your dex (+/- any
				penalties/bonuses), and the monster does the same. As the attacker,
				if the random number picked is higher than the monster's random
				number, it is considered a hit. This same process is repeated for
				when monsters attack you. If the monster rolls a higher number than
				you (0 to dex+/-modifiers) then the monster successfully hits.
			</p>

			<h4>Intelligence</h4>
			<p>
				<ul>
					<li><i>(Not yet implemented)</i> It will heavily influence your character's ability to invent a new skill/build-plan</li>
					<li>Every point after 4 gives 2.5% extra critical chance.</li>
				</ul>
			</p>

			<h2>Improving character stats</h2>
			<p>
				Every hit attempt (missing and blocked attacks included) increases all stats, some increase faster than others.
			</p>

			<h2>Combat</h2>
			<p>
				A hit in combat is always followed by a counter from the monster. The odds of a hit is calculated as per the section above where <b>Dexterity</b> is talked about. At the moment, while in combat, you cannot do anything but attack or run away. In the future, I plan on implementing the ability to pick up stuff from the ground, but the monster will have an opportunity to hit you once. Equipping or de-equipping an item would also cause the monster to try a hit (but again, this is not yet implemented). Additionally, if you do run away and reenter the combat site you will engage in combat with the monster in that site.
			</p>
			<p>
				Strength adds to how much damage you do per hit. The strength damage you do is NOT included in the crit multiplier, so when a crit occurs the damage that is multiplied is the base weapon damage only. Additionally, 2 handed weapons give a 1.5x strength damage bonus over 1 handed weapons.
			</p>

			<h2>Equipment</h2>
			<p>
				Every equipment has the following stats.
				<ul>
					<li>Dexterity Penalty: This is subtracted from your total dexterity. If the value is negative it instead buffs you instead.</li>
					<li>Strength Requirement: This states how much strength it's needed to wear that equipment.</li>
					<li>Block Chance: The probability of it blocking a hit when attacked. (More explained below)</li>
					<li>Damage Reduction: If a hit is blocked, this number is subtracted from it. (More explained below)</li>
					<li>Block bludgeoning / piercing / slashing: Explained in depth below</li>
					<li>Weigth: How much it weights (this limits how many can be put in a chest or can be carried).</li>
					<li>Space: Limites how many can be put in a chest.</li>
					<li>Warmth: Not in use at the moment.</li>
					<li>Durabilty: Every use decreases it by 1. When it gets to -1 the equipment is gone.</li>
				</ul>
			</p>

			<h3>Armor</h3>
			<p>
				When a character (or monster) attempts a hit the game will first determine which body part is going to be hit. The odds of given body part being targeted is as foll 
				<ul>
					<li>Chest/Shirt: 50%</li>
					<li>Legs: 30%</li>
					<li>Head: 10%</li>
					<li>Hands: 5%</li>
					<li>Feet: 5%</li>
				</ul>
			</p>

			<p>
				If the hit ends up targeting a body-part/slot that is not equipped with anything, the hit will always succeed. Otherwise, the odds of the armor blocking the hit is based on the armor's block chance (which is loosely based on how much of the body part the armor covers effectively). Damage reduction will reduce the damage from a hit by the amount shown on the piece of equipment being hit. Furthermore, if the  incoming attack is a "bludgeoning" type, and the armor is "EXCELLENT"  blocking against bludgeoning, the amount of damage reduction is doubled. Here is a list of how the blocking ability property affects the damage reduction:
				<ul>
					<li>Excellent = 2x the listed damage reduction (double the damage reduction)</li>
					<li>Good = 1.5x the listed damage reduction</li>
					<li>Average = 1x the listed damage reduction (no change)</li>
					<li>Poor = 0.75x the listed damage reduction</li>
					<li>Minimal = 0.5x the listed damage reduction (half the damage reduction)</li>
					<li>None = No damage reduction occurs (the block just fails)</li>
				</ul>
				Because of the block capabilities, different types of weapons will be more effective against certain monsters.
			</p>

			<h3>Multiple damage types on weapons</h3>
			<p>
				Some weapons such as Macuiahuitl have two damage types (bludgeoning and piercing). This means every hit it will deal the best choice.
				If for example it is blocked by a Full-Plate (Minimal to piercing) and also a Mithril Chain shirt (Excellent to piercing but minimal to bludgeoning), it will deal piercing to the plate and bludgeoning to the shirt.
			</p>

			<h3>Weapons and shield blocking</h3>
			<p>
				Hand equipments such as weapons and shields rolls to block BEFORE any other armor piece. The block % shows the chance it has of blocking any incoming attack, regardless of where it will hit.<br>
				2 Handed equipment rolls individually twice. So a greatsword with 30% block chance rolls twice for 30% instead of 60%.
			</p>

			<h3>Critical hits</h3>
			<p>
				When an attack succeeds, the weapon's critical hit chance is rolled to see if the attack is a critical hit. If it succeeds, the weapon damage is simply multiplied by the critical multiplier on the attacking weapon.
			</p>

			<h3>Dual Wielding</h3>
			<p>
				When using two weapons, the offhand (the one you didn't attack with) has a chance to give an extra hit on top of the regular one. That chance is equal to the final critical chance (weapon chance + int bonus).<br>
				The hit is just like a regular one and counts as an individual hit (damage doesn't stack). So in simple words, you attack twice on the same turn.
			</p>

			<h3>Sample attack, order of operations...</h3>
			<ol>
				<li>Both characters roll a random number between 0 and the character's dexterity plus any modifiers (like armor dexterity penalty). If the attacker rolls a higher number than the defender, the attack is a hit</li>
				<li>The game determines if a critical hit has occurred based on the attacking weapon's critical hit chance. If it is a critical hit, damage is multiplied by critical multiplier.</li>
				<li>If the left or right hand contains a piece of equipment with a block chance, we randomly determine if the block succeeds based entirely on that block chance. If the block is successful, the damage is reduced by the damage reduction on the blocking equipment  and it is at this point that the damage reduction is adjusted depending on the type of attack. If the attacking weapon has more than one type of damage, the damage that will be MOST effective against the armor will be used.</li>
				<li>The game randomly determines which body part is to be hit based on the percentages shown above.</li>
				<li>If there is an armor on the body part that is being hit, we randomly determine if the block succeeds based entirely on that armor's block chance. If the block is successful, the damage is reduced by the damage reduction on the blocking equipment  and it is at this point that the damage reduction is adjusted depending on the type of attack. If the attacking weapon has more than one type of damage, the damage that will be MOST effective against the armor will be used.</li>
				<li>If no blocks occurred, or if the damage reduction was not enough to completely negate the damage done, damage is then dealt to the defender.</li>
			</ol>

			<h3>Ranged Weapons</h3>
			<p>
				<i>At the moment, ranged weapons are not implemented (but they will still work as a regular melee weapon).</i> In the not too distant future, range weapons will work by starting the opposing monster at a random distance/range (with ranges influenced by the location [eg. forests will generally be short range, wide open flat lands would be long range]). The monster will have a set land speed and you will have the opportunity to attack the monster with your range weapon until the monster's distance is close enough to you. The number of times you can attack will depend on you weapon's range, the speed of the monster, and the distance the monster started at (which is random, but influenced by the terrain).
			</p>

			<h2>Death</h2>
			<p>
			When the hp of a player goes below 1 they roll a dice and can die instantly (have to start over) or become unconscious, which means they can be rescued.<br>
			Roughly every 10 minutes the unconscious player rolls a check to see if they die or stay that way.<br>
			<strong>For premium players</strong>If their body is found and brought to a resting spot (camps, inns or houses) they get healed to 1 hp. Non-premium have a chance to upgrade while unconscious and be saved.<br>
			<strong>Upon going below 1 hp all items are dropped on the combat site so make sure to store them in houses</strong>
			</p>

			<h2>Houses</h2>
			<p>
			Players can spend 2.000 gold to buy a house from any major city Town Hall.<br>
			Upon buying a link appears on the city location and there they can store items and gold (a chest or coin pouch is needed).<br>
			Any player brought into the house via partying or if they see a house link shared gets access to it so make sure to keep it safe.<br>
			<strong>Premium players never forget house paths on death</strong>
			</p>


			<h2 id='parties'>Parties</h2>
			<p>Players are allowed to party with up to 3 other people. When a player is in a party
				as the party leader, he will have full control over where the party goes. The rest of 
				the party simply follows the leader around.
			</p>
			<p>
				<i>This can be useful for ferrying/taxiing people around the world too.</i>
			</p>
			<p>
				When the party enters a new location, all members of the party will automatically discover
				the path to that location so they can return to it on their own later. <strong>Be very
				careful when you are the party leader because if you accidentally go to your house
				you would be giving every party member access to it permanently!</strong><br>
			</p>

			<h3>Party combat</h3>
			<p>When the leader discovers a monster to fight, all party members are automatically entered into combat with the same monster. All members can attack the monster and just like regular combat, each attack will be followed by the monster's counter attack. Only the party leader can run from the monster, if non-leading members try to run from the fight, they are met with an error message. They can however leave the party first before attempting to run. If the leader were to die during combat, he is removed from the party and someone else then becomes the party leader.
			</p>
			<p>Parties are NOT able to attack defence structures. Player or NPC owned structures used in territory control utilize a 1 on 1 combat system which requires that all attacking characters must leave their party before they approach a defence structure. <a href='#defencestructures'>Click here for more information on defence structures.</a>
			</p>

			<h3>Raid bosses</h3>
			<p>
				We have been experimenting with having regular one-off events where players battle a unique foe. Usually this is some monster with more hitpoints than any one character can handle and it requires the cooperation of the entire server to take them down. Events may be announced on reddit, global chat (via red color text) or simply be unnanounced and up to players to stumble upon it.<br> Quite often bosses also drop unique equipment that cannot be obtained in any other way.
			</p>

			<h2 id='duels'>Dueling</h2>		
			<p>
			Disabled at the moment
			</p>

			<h2 id='groups'>Groups</h2>
			<p>
			Players can create groups that acts as guilds, with it they can choose a group name and titles to the members, as well as assigning people to be admin and own group houses which are shared among all the members.
			</p>

			<h2 id='defencestructures'>Defence Structures and Territory Control</h2>
			<p>Will be reworked with new combat system</p>
			
			<br>
			<a href='login.jsp' class='main-button'>Login or signup
			to play right now</a>
			<br>
		</div>
	</div>
</body>
</html>
