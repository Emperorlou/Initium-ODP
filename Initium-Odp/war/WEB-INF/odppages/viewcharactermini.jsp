<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<div id='reload-div-return'></div>
<div id='reload-div'>
	<div style='max-width:320px;'>
		<c:if test="${type == 'PC' }">
			<div class='mini-window-header'>
				<c:if test="${type == 'PC' }">
					<div class='mini-window-header-split'>
						<a onclick='$(".cluetip").hide(); joinPartyCharacter(event, ${characterIDKey})'>Join Party</a><br><br>
						<a onclick='$(".cluetip").hide(); setPrivateChatTo("${characterNameStr}", ${characterIDKey})'>Private Chat</a><br><br>
						<a onclick='$(".cluetip").hide(); ignoreAPlayer(${characterID}, "${characterNameStr}");'>Ignore</a>
					</div>
				</c:if>
				
				
				<div class='mini-window-header-split' style='text-align:right'>
					<c:if test="${type == 'PC' }">
						<a onclick='tradeStartTradeNew(event, ${characterIDKey})'>Request Trade</a><br><br>
						<a onclick='duelRequest(${characterIDKey});'>Request Duel</a><br><br>
						<a onclick='$(".cluetip").hide(); viewStore(${characterID});'>View Store</a>
						<c:if test="${isPremium==false}">
							<br><br><a onclick='giftPremium("${characterName}")'>Gift Premium</a>
						</c:if>
					</c:if>
				</div>
			</div>
		</c:if>
				
		
		<c:if test="${isPartied==true}"> 
			<div class='main-partied-indicator'>PARTIED</div>
		</c:if>	
		<div class='normal-container'>${characterWidget}</div> 
		<div class='normal-container'>
		<c:if test="${isCloaked==false || (isCloaked==true && isSelf==true)}">
			<c:if test="${buffs==true}">
				<div class='buff-pane' onclick='$(this).hide(); $("#buffsList").show();'>
				${printBuff}
				</div>
					<div id='buffsList' style='display:none;'>
					<h5>Buffs/debuffs</h5>
					${buffList}
					</div>
			</c:if>
			<c:if test="${hasAchievements==true}">
				<div></div>
				<div class='buff-pane' onclick='$(this).hide(); $("#achievementsList").show();'>
				${printAchievement}
				</div>
					<div id='achievementsList' style='display:none;'>
					<h5>Achievements</h5>
					${achievementList}
					</div>
			</c:if>
			<div><h5>Stats</h5>
				<div>Strength: <div class='main-item-subnote' name='strength'>${getStrength} (${characterStrength})</div></div>
				<div>Dexterity: <div class='main-item-subnote' name='dexterity'>${getDexterity} (${characterDexterity})</div></div>
				<div>Intelligence: <div class='main-item-subnote' name='intelligence'>${getIntelligence} (${characterIntelligence})</div></div>
				<c:if test="${isSelf==true}">
					<br>
					<div name='inventoryWeight'>${inventoryWeight}</div>
				</c:if>
				<div><h5>Equipment</h5>
				${equipList}
				</div>
			</div>
		</c:if>			

	</div>
	<div class='normal-container'>
		<c:if test="${(isCloaked==false && type=='PC') || (isCloaked==true && isSelf==true)}">
			<h5>Referral Stats</h5>
			<div style='margin-left:10px'>
				<p>
					Premium gifts given: <span name='premiumGiftsGiven'>${premiumGiftsGiven}</span>
				</p>
				<p>
					Referral views: <span name='referralViews'>${referralViews}</span><br>
					Referral signups: <span name='referralSignups'>${referralSignups}</span><br>
					Referral donations: <span name='referralDonations'>${referralDonations}</span>
				</p>
			</div>
		</c:if>		
		<c:if test="${isCloaked==true}">
			<br>
			<div class='item-flavor-description'>
				This character is cloaked. All stats and equipment are hidden from other players.
			</div>
		</c:if>
	</div>
</div>
</div>
